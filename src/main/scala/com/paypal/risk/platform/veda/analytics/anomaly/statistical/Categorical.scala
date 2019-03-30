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

import scala.collection.mutable

object Categorical {

  class AggregationFactory[T] extends DataAggregationFactory {
    override def createNewAggregation(): DataAggregation = new Aggregation[T]
  }

  class Aggregation[T] extends DataAggregation {
    val map: mutable.Map[T, Long] = mutable.Map[T, Long]().withDefaultValue(0)

    override def update(trackedMetric: TrackedMetric): Unit = {
      val value = trackedMetric.value.asInstanceOf[T]
      map.put(value, map.getOrElse(value, 0L) + 1)
    }

    override def toString: String = Utils.mapToString(map.toMap, "aggregation")
  }

  class Detection[T](refAggrFunc: Seq[Map[T, Long]] => Map[T, Long],
    statisticalFunc: (Map[T, Long], Map[T, Long]) => Functions.StatisticalResult[T],
    anomalyEvaluationFunc: Functions.StatisticalResult[T] => Boolean)
    extends DetectionModel {

    override def detect(aggr: DataAggregation, refAggr: Seq[DataAggregation]): Option[Report] = {
      val map = aggr.asInstanceOf[Aggregation[T]].map.toMap

      val refMaps = refAggr.map { aggr => aggr.asInstanceOf[Aggregation[T]].map.toMap }
      if (refAggr.isEmpty) {
        Option(DistributionReport[T](false, None, map, Map.empty))
      } else {
        val refCalculatedMap = refAggrFunc(refMaps)
        val stat = statisticalFunc(map, refCalculatedMap)
        Option(DistributionReport[T](anomalyEvaluationFunc(stat), Some(stat), map, refCalculatedMap))
      }
    }
  }

  case class DistributionReport[T](isAnomaly: Boolean,
    statisticalRes: Option[Functions.StatisticalResult[T]],
    currentDist: Map[T, Long],
    refDist: Map[T, Long]) extends Report {

    override def toString: String = {

      val evalStr = statisticalRes.map(_.toString)
        .getOrElse("couldn't evaluate statistical method without a reference distribution")

      val stringBuilder = new mutable.StringBuilder()
      stringBuilder.append("\n==================================================================")
      stringBuilder.append("\n Distribution Report")
      stringBuilder.append(s"\n   Detected Anomaly: $isAnomaly\n")
      stringBuilder.append(s"\n Explain: $evalStr\n")
      stringBuilder.append(Utils.mapToString(currentDist, "Current Distribution"))
      stringBuilder.append(Utils.mapToString(refDist, "Reference Distribution"))
      stringBuilder.append(Utils.refTimeRangesString(this))
      stringBuilder.append("==================================================================\n")
      stringBuilder.toString()
    }
  }

}
