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

import com.paypal.risk.platform.veda.analytics.anomaly.{AnomalyPipeline, DataAggregationFactory, DetectionModel}
import com.paypal.risk.platform.veda.analytics.anomaly.clustering.{Clustering, DensityClustering}
import com.paypal.risk.platform.veda.analytics.anomaly.raw.ContainerType
import com.paypal.risk.platform.veda.analytics.anomaly.window.{Window, WindowStrategy}
import com.paypal.risk.platform.veda.analytics.anomaly.windowref.{WindowReference, WindowRefStrategy}

import scala.collection.mutable

object ClusteringPipelineBuilder {
  def apply(): ClusteringPipelineBuilder = new ClusteringPipelineBuilder()
}

class ClusteringPipelineBuilder {

  private val columns = mutable.Buffer[String]()
  private var windowing = Window.global
  private var windowRef = WindowReference.NOREF

  def onColumns(cols: Seq[String]): ClusteringPipelineBuilder = {
    columns.appendAll(cols)
    this
  }

  def setWindowing(windowStrategy: WindowStrategy): ClusteringPipelineBuilder = {
    windowing = windowStrategy
    this
  }

  def setWindowReferencing(windowReference: WindowRefStrategy): ClusteringPipelineBuilder = {
    windowRef = windowReference
    this
  }

  def buildDensityClusteringModel[T, U](
    clusteringFunc: (Seq[Array[Double]], Seq[Seq[Array[Double]]]) => Clustering,
    maxWindowCapacity: Int = DensityClustering.DEFAULT_CAPACITY,
    containerType: ContainerType = DensityClustering.DEFAULT_CONTAINER_TYPE): Seq[AnomalyPipeline] = {

    val detectionModel = new DensityClusteringModel(clusteringFunc)
    val aggregationFactory = new DensityClustering.AggregationFactory(containerType, Some(maxWindowCapacity))
    buildModel(detectionModel, aggregationFactory)
  }

  def buildModel(detectionModel: DetectionModel, aggregationFactory: DataAggregationFactory):
  Seq[AnomalyPipeline] = {
    require(columns.nonEmpty, "no columns found for pipeline creation")
    columns.map { col =>
      AnomalyPipeline(col, windowing, windowRef, aggregationFactory, detectionModel)
    }
  }
}
