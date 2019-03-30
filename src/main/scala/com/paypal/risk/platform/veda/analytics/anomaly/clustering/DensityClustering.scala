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
package com.paypal.risk.platform.veda.analytics.anomaly.clustering

import com.paypal.risk.platform.veda.analytics.anomaly.{DataAggregation, DataAggregationFactory, DetectionModel,
  Report, TrackedMetric, Utils}
import com.paypal.risk.platform.veda.analytics.anomaly.raw._

import scala.collection.mutable

object DensityClustering {

  val DEFAULT_CAPACITY = 10000
  val DEFAULT_CONTAINER_TYPE: ContainerType = UniformSampling

  class AggregationFactory(
    containerType: ContainerType = DEFAULT_CONTAINER_TYPE,
    maxWindowCapacity: Option[Int] = Some(DEFAULT_CAPACITY))
    extends DataAggregationFactory {

    /** creates a new data aggr when required by window strategy */
    override def createNewAggregation(): DataAggregation = {
      val container = containerType match {
        case UniformSampling =>
          require(maxWindowCapacity.isDefined, "max capacity must be set for UniformSampling aggregation")
          new UniformSamplingContainer[Array[Double]](maxWindowCapacity.get)
        case SortedUniformSampling =>
          require(maxWindowCapacity.isDefined, "max capacity must be set for SortedUniformSampling aggregation")
          new SortedUniformSamplingContainer[Array[Double]](maxWindowCapacity.get)
        case Raw =>
          require(maxWindowCapacity.isEmpty, "max capacity must not be set for Raw aggregation")
          new RawContainer[Array[Double]]
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
   * @param container container of the metric of data
   */
  class Aggregation(val container: BaseContainer[Array[Double]]) extends DataAggregation {
    var candidatePoints = new BlockList[Array[Double]]()

    override def update(trackedMetric: TrackedMetric): Unit = {
      val point = trackedMetric.value.asInstanceOf[Seq[Double]].toArray
      container.update(point)
      candidatePoints.append(point)
    }

    // remove candidate points when not needed anymore
    override def cleanAndCompact(): Unit = {
      candidatePoints = new BlockList[Array[Double]]()
    }

    override def toString: String =
      s"""
        | stored reference values: ${container.size}
        | pending candidate points: ${candidatePoints.size}
      """.stripMargin
  }


  /**
   * compares current points with all previous points in ref aggregations,
   * and using a clustering function to detect outliers
   *
   * @param clusteringFunc function that takes candidatePoints and reference points
   *                       then returns the outlier points
   */
  class Detection(
    clusteringFunc: (Seq[Array[Double]], Seq[Seq[Array[Double]]]) => Seq[Array[Double]])
    extends DetectionModel {

    override def detect(aggr: DataAggregation, refAggr: Seq[DataAggregation]): Option[Report] = {

      val currentAggr = aggr.asInstanceOf[Aggregation]
      val candidatePoints = currentAggr.candidatePoints
      currentAggr.cleanAndCompact()
      val refPoints = refAggr.map(_.asInstanceOf[Aggregation].container.getValues)
      val anomalousValues = clusteringFunc(candidatePoints, refPoints)

      val refPointsCount = refPoints.map(_.size).sum
      Option(ClusterReport(anomalousValues,
        candidatePoints.size, refPointsCount))
    }
  }

  case class ClusterReport(anomalies: Seq[Array[Double]], pointsCount: Long, refPointsCount: Long)
    extends Report {

    override def toString: String = {
      val isAnomaly = anomalies.nonEmpty && refPointsCount > 0
      val evalStr = if (isAnomaly) {
        s"anomalous values detected:\n${anomalies.map(_.mkString(",")).mkString("\n")}"
      }
      else "not anomalous values detected"

      val stringBuilder = new mutable.StringBuilder()
      stringBuilder.append("\n==================================================================")
      stringBuilder.append("\n Density Clustering Report")
      stringBuilder.append(s"\n Detected Anomaly: $isAnomaly\n")
      stringBuilder.append(s"\n Checked: $pointsCount points against $refPointsCount references points\n")
      stringBuilder.append(s"\n Explain: $evalStr\n")
      stringBuilder.append(Utils.refTimeRangesString(this))
      stringBuilder.append("==================================================================\n")
      stringBuilder.toString
    }
  }

}
