/**
 * Copyright 2019 PayPal Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.paypal.risk.platform.veda.analytics.anomaly

import java.sql.Timestamp

import com.typesafe.scalalogging.LazyLogging
import org.apache.spark.sql.{DataFrame, Dataset}
import org.apache.spark.sql.streaming.{GroupState, GroupStateTimeout}

import scala.collection.mutable

object AnomalyWorkload {
  def builder(): AnomalyWorkloadBuilder = {
    new AnomalyWorkloadBuilder
  }
}

case class AnomalyWorkload(pipelines: Seq[AnomalyPipeline], tsField: String, watermark: String,
  partitioners: Seq[ColumnPartitioner] = Seq.empty) extends LazyLogging {

  /** the watermark will be used by default to remove aggregation from state when no refWatermark was defined */
  private val watermarkMillis = Utils.durationToMillis(watermark)

  /** apply this workload on a given DataFrame */
  def process: DataFrame => Dataset[(ColumnKey, Report, WindowRange)] = workload

  /** used for separating between column and window key when combined e.g. age#983 */
  private val KEY_SEPARATOR = "#"

  /** locally mark empty AggregationKey */
  private val EMPTY_AGGREGATION_KEY = "EMPTY_AGGREGATION_KEY"

  /** managed aggregation metadata fields keys */
  val AGGR_META_KEY = "AGGR_KEY"
  val AGGR_META_COL_KEY = "AGGR_META_COL_KEY"
  val AGGR_META_TIME_RANGE = "TIME_RANGE"

  /**
   * internal maps from column key to relevant pipeline components,
   * we decompose the pipelines for efficient processing
   */
  private val pipeKeyToColumn =
    pipelines.map(p => (p.id, p.colKey)).toMap

  private val pipeKeyToDataModelFactory =
    pipelines.map(p => (p.id, p.aggrFactory)).toMap

  private val pipeKeyToAnomalyModel =
    pipelines.map(p => (p.id, p.anomalyModel)).toMap

  private val pipeKeyToWindowStrategy =
    pipelines.map(p => (p.id, p.windowStrategy)).toMap

  private val pipeKeyToWindowRefStrategy =
    pipelines.map(p => (p.id, p.windowRefStrategy)).toMap

  private val colNameToPartitioner = partitioners.map(p => p.colName -> p).toMap

  private var shouldScanHistory = false

  /**
   * activating this mode will run anomaly detection not only on the latest aggregation but also on all
   * previous aggregations. this will also cancel historical aggregations cleanup mechanism.
   * the mode was designed to improve batch and research experience,
   * using this mode in production is highly discouraged as it can impact performance significantly
   */
  def withFullHistoryScan: AnomalyWorkload = {
    logger.warn("Full history scan detected, using this mode in production is highly discouraged")
    shouldScanHistory = true
    this
  }


  /**
   * split each row in a user provided DataFrame to a separate metric (per pipeline)
   */
  private def splitToTrackedMetricsDataset(df: DataFrame): Dataset[InternalTrackedMetric] = {

    val pipeKeys = pipelines.map(_.id)

    df.withWatermark(tsField, watermark)
      .flatMap { row =>
        pipeKeys.map { pipeKey =>
          val colName = pipeKeyToColumn(pipeKey)
          val value = row.getAs[Any](colName)
          val shard = colNameToPartitioner.get(colName).map(_.shard(value)).getOrElse(0)
          InternalTrackedMetric(InternalMetricKey(pipeKey, shard),
            TrackedMetric(colName, value, row.getAs[Timestamp](tsField)))
        }
      }
  }

  /**
   * we use a concatenation of the the column with the event-time window,
   * as key for grouping metrics e.g. age#394448
   * each metric can belong to multiple time windows
   */
  private def getAggregationKeys(metric: InternalTrackedMetric): Seq[AggregationKey] = {
    pipeKeyToWindowStrategy(metric.internalKey.pipelineKey)
      .extractTimeWindowKeys(metric.metric.ts)
      .map { windowKey => composeAggregationKey(metric.internalKey, windowKey) }
  }

  /**
   * duplicate each metric and add an aggr key to it, for each window the metric belongs to
   */
  private def duplicatePerTimeWindowWithKey(metric: InternalTrackedMetric): Seq[KeyedTrackedMetric] = {
    getAggregationKeys(metric).map { aggrKey => KeyedTrackedMetric(aggrKey, metric) }
  }

  private def composeAggregationKey(internalKey: InternalMetricKey, windowKey: WindowKey): AggregationKey =
    s"${internalKey.pipelineKey}$KEY_SEPARATOR${internalKey.shard}$KEY_SEPARATOR$windowKey"

  /** decompose group key to its components */
  private def decomposeAggregationKey(key: AggregationKey): (PipelineKey, ShardKey, WindowKey) = {
    val splits = key.split(KEY_SEPARATOR)
    (splits(0), splits(1).toInt, splits(2))
  }

  private def extractRefTimeout(pipeKey: PipelineKey, aggKey: AggregationKey): Long = {
    if (aggKey == EMPTY_AGGREGATION_KEY) {
      Long.MinValue
    } else {
      pipeKeyToWindowStrategy(pipeKey).getTimeoutTimestamp(decomposeAggregationKey(aggKey)._3).getOrElse(Long.MaxValue)
    }
  }

  private def extractRefStartTime(pipeKey: PipelineKey, aggKey: AggregationKey): String = {
    pipeKeyToWindowStrategy(pipeKey).getStartTimestamp(decomposeAggregationKey(aggKey)._3)
      .map(ts => new Timestamp(ts).toString).getOrElse("not_defined")
  }

  private def getAggrTimeRange(pipeKey: PipelineKey, aggKey: AggregationKey): String = {
    val start = extractRefStartTime(pipeKey, aggKey)
    val end = new Timestamp(extractRefTimeout(pipeKey, aggKey)).toString
    s"from $start to $end"
  }

  private def createNewAggregation(pipeKey: PipelineKey, aggKey: AggregationKey): DataAggregation = {
    val agg = pipeKeyToDataModelFactory(pipeKey).createNewAggregation()
    agg.metadata.put(AGGR_META_TIME_RANGE, getAggrTimeRange(pipeKey, aggKey))
    agg.metadata.put(AGGR_META_KEY, aggKey)
    agg.metadata.put(AGGR_META_COL_KEY, pipeKeyToColumn(pipeKey))
    agg
  }


  /**
   * here we create, update and clear each metric group's in-memory state.
   *
   * timeout: window strategy controls if a state has a timeout,
   * and control the timeout timestamp.
   * when the watermark pass this timestamp the state is considered timed-out
   *
   * @param pipeKey the group key e.g. age#394448
   * @param events  new metrics added to the group, including late events.
   *                can be empty in case of state timeout.
   *                note: since this is an iterator we can scan the collection only once
   * @param state   spark managed group state
   * @return pair of column key and current data window state, for further processing.
   *         optional so we can avoid processing when the method is activated due to state timeout
   */
  private def updateGroupWindowState(
    internalKey: InternalMetricKey,
    events: Iterator[KeyedTrackedMetric],
    state: GroupState[WindowState]): Option[KeyedAggregation] = {

    // for logging
    val pipeKey = internalKey.pipelineKey
    val shard = internalKey.shard
    val colKey = pipeKeyToColumn(pipeKey)
    // if state doesn't exist initialize it
    val windowState = state.getOption.getOrElse {
      WindowState(EMPTY_AGGREGATION_KEY, mutable.Map[AggregationKey, DataAggregation]())
    }

    // update each group data model with new events and the maxRefTimeout
    var updatedCurrentAggrKey = windowState.currentAggrKey
    var eventsCount = 0
    events.foreach(event => {
      eventsCount += 1
      if (!windowState.refs.contains(event.aggregationKey)) {
        logger.info(s"pipeline: $pipeKey($colKey) shard: $shard, created new aggregation: ${event.aggregationKey}")
        windowState.refs.put(event.aggregationKey, createNewAggregation(pipeKey, event.aggregationKey))

      }
      windowState.refs(event.aggregationKey).update(event.metric.metric)
      if (extractRefTimeout(pipeKey, event.aggregationKey) > extractRefTimeout(pipeKey, updatedCurrentAggrKey)) {
        updatedCurrentAggrKey = event.aggregationKey
      }
    })

    val currentBucketTimeout = extractRefTimeout(pipeKey, updatedCurrentAggrKey)

    // a timeout should never occur this is here for debugging in case it does
    if (state.hasTimedOut) {
      logger.warn(s"an unexpected state timeout occurred:\n " + groupStateReferencesLogStr(pipeKey, colKey, shard,
        updatedCurrentAggrKey, currentBucketTimeout, windowState.refs.toMap))
    }

    state.update(windowState.copy(currentAggrKey = updatedCurrentAggrKey))


    // todo: this can be optimized a. not to run every trigger, b. use a sorted map
    // clean reference buckets older than currentBucketTimeout - refWatermark, cancel if in history scan mode
    val refWatermark = pipeKeyToWindowRefStrategy(pipeKey).watermarkMilli.getOrElse(watermarkMillis)
    if (!shouldScanHistory) {
      windowState.refs.keys
        .filter(aggrKey => extractRefTimeout(pipeKey, aggrKey) < currentBucketTimeout - refWatermark)
        .foreach { aggrKey =>
          windowState.refs.remove(aggrKey).foreach(_ => logger.info(s"clearing old aggregation $aggrKey"))
        }
    }

    logger.info(
      s"pipeline: $internalKey($colKey), updated with $eventsCount new events, current aggregation:" +
        s"$updatedCurrentAggrKey")
    logger.debug(groupStateReferencesLogStr(pipeKey, colKey, shard, updatedCurrentAggrKey, currentBucketTimeout,
      windowState.refs.toMap))

    Option(KeyedAggregation(internalKey, updatedCurrentAggrKey, windowState.refs.toMap))

  }

  private def groupStateReferencesLogStr(pipeKey: PipelineKey, colKey: ColumnKey, shard: ShardKey,
    currentAggrKey: AggregationKey, timeout: Long, refs: Map[AggregationKey, DataAggregation]): String = {
    s"""\n
      |  pipeline: $pipeKey
      |  column: $colKey
      |  shard: $shard
      |  currentAggr: $currentAggrKey
      |  currentTimeRange: ${getAggrTimeRange(pipeKey, currentAggrKey)}
      |  refs: \n\n${refs.map { case (_, aggr) => referenceLogStr(aggr) }.mkString("\n")}\n
    """.stripMargin
  }

  private def referenceLogStr(aggr: DataAggregation): String = {
    s"""
      | aggregationKey: ${aggr.metadata.get(AGGR_META_KEY)}
      | timeRange: ${aggr.metadata.get(AGGR_META_TIME_RANGE)}
      | ${aggr.toString}
      """.stripMargin
  }

  /** for a given aggr key e.g. age#341 get its relevant reference aggr keys e.g. age#340 */
  private def getRefAggregationKeys(aggrKey: AggregationKey): Seq[String] = {
    val (pipeKey, shard, windowKey) = decomposeAggregationKey(aggrKey)
    val window = pipeKeyToWindowStrategy(pipeKey)
    val refStrategy = pipeKeyToWindowRefStrategy(pipeKey)
    val computedRefWindowKeys = refStrategy.getReferenceWindowKeysFunc(window)(windowKey)
    val refKeys = if (refStrategy.currentWindowIncluded) Seq(aggrKey) ++ computedRefWindowKeys
    else computedRefWindowKeys
    refKeys.map(windowKey => composeAggregationKey(InternalMetricKey(pipeKey, shard), windowKey))
  }

  /** use anomaly model to detect anomalies in data aggrs and produce reports */
  private def applyAnomalyModel(aggr: KeyedAggregation) = {
    if (shouldScanHistory) applyAnomalyModelOnAllRefs(aggr)
    else Iterable(applyAnomalyModelOnCurrentWindow(aggr)).flatten
  }

  /** use anomaly model to detect anomalies in data aggrs and produce reports */
  private def applyAnomalyModelOnCurrentWindow(aggr: KeyedAggregation): Option[(ColumnKey, Report, WindowRange)] = {
    val anomalyModel = pipeKeyToAnomalyModel(aggr.internalKey.pipelineKey)
    val refAggr = getRefAggregationKeys(aggr.currAggrKey).flatMap(aggr.refs.get)
    val refTimeRanges = refAggr.map(_.metadata(AGGR_META_TIME_RANGE))
    aggr.refs.get(aggr.currAggrKey)
      .flatMap { currentAgg =>
        anomalyModel.detect(currentAgg, refAggr)
          .map(_.setRefTimeRanges(refTimeRanges))
          .map(_.setCurrentTimeRange(currentAgg.metadata(AGGR_META_TIME_RANGE)))
      }
      .map { report =>
        (pipeKeyToColumn(aggr.internalKey.pipelineKey), report, report.getCurrentTimeRange)
      }
  }

  /** use anomaly model to detect anomalies in data aggrs and produce reports */
  private def applyAnomalyModelOnAllRefs(aggr: KeyedAggregation): Iterable[(ColumnKey, Report, WindowRange)] = {
    aggr.refs.keys.flatMap { aggKey =>
      val newAgg = KeyedAggregation(aggr.internalKey, aggKey, aggr.refs)
      applyAnomalyModelOnCurrentWindow(newAgg)
    }
  }

  /**
   * the anomaly detection processing, lazy and will run when the DataFrame is triggered
   * steps:
   * 1. split micro batch to tracked metrics
   * 2. duplicate each metric to the window it belongs to
   * 3. group each metric by "col#window" key e.g. age#13782
   * 4. update each groups data window and in-memory state
   * 5. apply anomaly detection model on data windows
   * 6. apply anomaly handler logic on reports
   */
  private def workload(df: DataFrame): Dataset[(ColumnKey, Report, WindowRange)] = {
    import df.sparkSession.implicits._

    logger.info(s"executing workload with the following pipelines:\n${pipelines.mkString("\n")}")

    splitToTrackedMetricsDataset(df)
      .flatMap {
        duplicatePerTimeWindowWithKey
      }
      .groupByKey {
        _.metric.internalKey
      }
      .mapGroupsWithState(GroupStateTimeout.ProcessingTimeTimeout()) {
        updateGroupWindowState
      }
      .flatMap(x => x)
      .flatMap {
        applyAnomalyModel
      }
  }
}

/** container class for spark managed in-memory group state - "live" aggr (withing watermark) and their refs) */
case class WindowState(currentAggrKey: AggregationKey, refs: mutable.Map[AggregationKey, DataAggregation])

/** internal key that is used for the group by operation. making sure every pipeline (shard) on the same executor */
case class InternalMetricKey(pipelineKey: PipelineKey, shard: ShardKey)

/** container class for holding current data aggr with its key (e.g. age#3445) and its references */
case class KeyedAggregation(internalKey: InternalMetricKey, currAggrKey: AggregationKey,
  refs: Map[AggregationKey, DataAggregation])

/** container class for holding tracked metrics with their key (e.g. age#3445) */
case class InternalTrackedMetric(internalKey: InternalMetricKey, metric: TrackedMetric)

/** container class for holding tracked metrics with their key (e.g. age#3445) */
case class KeyedTrackedMetric(aggregationKey: AggregationKey, metric: InternalTrackedMetric)

case class ColumnPartitioner(colName: String, numShards: Int, partitionFunc: Any => Int) {
  def shard(value: Any): Int = partitionFunc(value) % numShards
}
