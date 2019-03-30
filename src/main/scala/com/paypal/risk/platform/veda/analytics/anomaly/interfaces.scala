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

import scala.collection.mutable

/** factory for creating new data aggrs */
trait DataAggregationFactory extends Serializable {

  /** creates a new data aggr when required by window strategy */
  def createNewAggregation(): DataAggregation
}

/** modeling of the data inside each window, can contain any custom logic */
trait DataAggregation extends Serializable {

  val metadata: mutable.HashMap[String, String] = mutable.HashMap[String, String]()

  /** adds a new metric observation to the aggr, no order of metrics is guaranteed */
  def update(trackedMetric: TrackedMetric): Unit

  /**
   * cleans and compacts unneeded state,
   * can be used after detection or in the future automatically at the background
   */
  def cleanAndCompact(): Unit = {}
}

/** modeling of when a data window model is considered an anomaly, can contain any custom logic */
trait DetectionModel extends Serializable {

  /** checks if aggr contains anomaly, None results are ignored */
  def detect(aggr: DataAggregation, refAggr: Seq[DataAggregation]): Option[Report]
}

/** response of the anomaly model check, even if no anomaly was detected, can have any logic */
trait Report extends Serializable {

  /** used internally to keep track of the actual time window when producing this report */
  private var currentTimeRange: String = "INFINITY"

  /** used internally to keep track of the actual time windows used in the reference calculation */
  private var referencesTimeRanges: Seq[String] = Seq.empty

  /** common functionally to allow users to fetch this information in their report */
  def getCurrentTimeRange: String = currentTimeRange

  private[anomaly] def setCurrentTimeRange(range: String): Report = {
    currentTimeRange = range
    this
  }

  /** common functionally to allow users to fetch this information in their report */
  def getRefTimeRanges: Seq[String] = referencesTimeRanges

  private[anomaly] def setRefTimeRanges(ranges: Seq[String]): Report = {
    referencesTimeRanges = ranges
    this
  }
}

/**
 * a single metric in the anomaly pipeline, dataframe rows are splitted into tracked metrics
 * according to pipeline definition
 *
 * @param colKey column name in the original dataframe
 * @param value  the value of the field in the original dataframe
 * @param ts     the event-time timestamp in the original dataframe
 */
case class TrackedMetric(colKey: ColumnKey, value: Any, ts: Timestamp) {
}

