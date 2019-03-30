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
package com.paypal.risk.platform.veda.analytics.anomaly.statistical

import com.paypal.risk.platform.veda.analytics.anomaly.{DataAggregation, DataAggregationFactory, DetectionModel, Report, TrackedMetric, Utils}
import com.paypal.risk.platform.veda.analytics.anomaly.raw._

import scala.collection.mutable

object Numerical {

  val DEFAULT_CAPACITY = 10000
  val DEFAULT_CONTAINER_TYPE: ContainerType = UniformSampling

  class AggregationFactory[T: Numeric](
    containerType: ContainerType = DEFAULT_CONTAINER_TYPE,
    maxWindowCapacity: Option[Int] = Some(DEFAULT_CAPACITY))
    extends DataAggregationFactory {

    /** creates a new data aggr when required by window strategy */
    override def createNewAggregation(): DataAggregation = {
      val container = containerType match {
        case UniformSampling =>
          require(maxWindowCapacity.isDefined, "max capacity must be set for UniformSampling aggregation")
          new UniformSamplingStatsContainer[T](maxWindowCapacity.get)
        case SortedUniformSampling =>
          require(maxWindowCapacity.isDefined, "max capacity must be set for SortedUniformSampling aggregation")
          new SortedUniformSamplingStatsContainer[T](maxWindowCapacity.get)
        case Raw =>
          require(maxWindowCapacity.isEmpty, "max capacity must not be set for Raw aggregation")
          new RawStatsContainer[T]
      }
      new Aggregation(container)
    }
  }


  /**
   * save all points in this window using specified container
   * Note: candidatePoints are freed from memory by detection function after they are evaluated for anomalies,
   * in some cases an aggregation might not be test for anomaly and then the candidatePoints will get freed from memory
   * only at aggregation timeout
   *
   * @param metricContainer container of the metric of data
   */
  class Aggregation[T](val statsContainer: StatisticsContainer[T]) extends DataAggregation {
    var candidatePoints = new BlockList[T]()

    override def update(trackedMetric: TrackedMetric): Unit = {
      statsContainer.update(trackedMetric.value.asInstanceOf[T])
      candidatePoints.append(trackedMetric.value.asInstanceOf[T])
    }

    // remove candidate points when not needed anymore
    override def cleanAndCompact(): Unit = {
      candidatePoints = new BlockList[T]()
    }

    override def toString: String =
      s"""
        | stored reference values: ${statsContainer.size}
        | pending candidate points: ${candidatePoints.size}
      """.stripMargin
  }


  class Detection[T, k, U](refAggrFunc: Seq[StatisticsContainer[T]] => k,
    statisticalFunc: (k, T) => U,
    anomalyEvaluationFunc: U => Boolean)
    extends DetectionModel {

    override def detect(aggr: DataAggregation, refAggr: Seq[DataAggregation]): Option[Report] = {

      val simpleAggr = aggr.asInstanceOf[Aggregation[T]]
      val points = simpleAggr.candidatePoints
      simpleAggr.cleanAndCompact()
      val refPoints = refAggr.map { aggr => aggr.asInstanceOf[Aggregation[T]].statsContainer }
      val anomalies = new mutable.ArrayBuffer[(T, U)]

      if (refAggr.nonEmpty) {
        val refCalculation = refAggrFunc(refPoints)
        points.foreach { p =>
          val stat = statisticalFunc(refCalculation, p)
          if (anomalyEvaluationFunc(stat)) {
            anomalies.append((p, stat))
          }
        }
      }
      val refPointsCount = refPoints.map(_.size).sum
      Option(DistributionReport[T, U](anomalies, points.size, refPointsCount))
    }
  }

  case class DistributionReport[T, U](anomalies: mutable.Buffer[(T, U)], pointsCount: Long, refPointsCount: Long)
    extends Report {

    override def toString: String = {

      val evalStr = if (anomalies.nonEmpty) s"data points evaluated as anomalies"
      else "no data points evaluated as anomalies"

      val stringBuilder = new mutable.StringBuilder()
      stringBuilder.append("\n==================================================================")
      stringBuilder.append("\n Distribution Report")
      stringBuilder.append(s"\n Detected Anomaly: ${anomalies.nonEmpty}\n")
      stringBuilder.append(s"\n Explain: $evalStr\n")
      stringBuilder.append(s"\n Checked: $pointsCount points against $refPointsCount references points\n")
      stringBuilder.append(Utils.mapToString(anomalies.toMap, "Anomalous Data Points"))
      stringBuilder.append(Utils.refTimeRangesString(this))
      stringBuilder.append("==================================================================\n")
      stringBuilder.toString()
    }
  }

}
