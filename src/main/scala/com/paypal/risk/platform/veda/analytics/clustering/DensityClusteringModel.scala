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
package com.paypal.risk.platform.veda.analytics.clustering

import com.paypal.risk.platform.veda.analytics.anomaly.{DataAggregation, DetectionModel, Report}
import com.paypal.risk.platform.veda.analytics.anomaly.clustering.Clustering
import com.paypal.risk.platform.veda.analytics.anomaly.clustering.DensityClustering.Aggregation

import scala.collection.mutable

/**
 * clusters all points in current aggregation using the given function
 *
 * @param clusteringFunc function that takes given input points
 */
class DensityClusteringModel(
  clusteringFunc: (Seq[Array[Double]], Seq[Seq[Array[Double]]]) => Clustering)
  extends DetectionModel {

  override def detect(aggr: DataAggregation, refAggr: Seq[DataAggregation]): Option[Report] = {
    val currentAggr = aggr.asInstanceOf[Aggregation]
    val latestWindowPoints = currentAggr.container.getValues
    currentAggr.cleanAndCompact()
    val refPoints = refAggr.map(_.asInstanceOf[Aggregation].container.getValues)
    val clustering = clusteringFunc(latestWindowPoints, refPoints)
    Option(FullClusteringReport(clustering))
  }
}

case class FullClusteringReport(clustering: Clustering)
  extends Report {

  override def toString: String = {
    val stringBuilder = new mutable.StringBuilder()
    stringBuilder.append("\n==================================================================")
    stringBuilder.append("\n Clustering Report")
    stringBuilder.append(s"\n Algorithm: ${clustering.getAlgName} \n")
    stringBuilder.append(s"\n Analyzed ${clustering.getLabels.length} points \n")
    stringBuilder.append(s"\n Detected ${clustering.getNumClusters} clusters \n")
    stringBuilder.append(s"\n Detected ${clustering.getNumNoisePoints} noise points\n")
    stringBuilder.append("==================================================================\n")
    stringBuilder.toString
  }
}
