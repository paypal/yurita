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

import com.paypal.risk.platform.veda.analytics.anomaly.clustering.DensityClustering
import com.paypal.risk.platform.veda.analytics.anomaly.raw.{ContainerType, StatisticsContainer}
import com.paypal.risk.platform.veda.analytics.anomaly.statistical.{Categorical, Functions, Numerical}
import com.paypal.risk.platform.veda.analytics.anomaly.window.{Window, WindowStrategy}
import com.paypal.risk.platform.veda.analytics.anomaly.windowref.{WindowReference, WindowRefStrategy}

import scala.collection.mutable

object PipelineBuilder {
  def apply(): PipelineBuilder = new PipelineBuilder()
}

class PipelineBuilder {

  private val columns = mutable.Buffer[String]()
  private var windowing = Window.global
  private var windowRef = WindowReference.NOREF

  def onColumns(cols: Seq[String]): PipelineBuilder = {
    columns.appendAll(cols)
    this
  }

  def setWindowing(windowStrategy: WindowStrategy): PipelineBuilder = {
    windowing = windowStrategy
    this
  }

  def setWindowReferencing(windowReference: WindowRefStrategy): PipelineBuilder = {
    windowRef = windowReference
    this
  }

  def buildCategoricalModel[T](refAggrFunc: Seq[Map[T, Long]] => Map[T, Long],
    statisticalFunc: (Map[T, Long], Map[T, Long]) => Functions.StatisticalResult[T],
    anomalyEvaluationFunc: Functions.StatisticalResult[T] => Boolean): Seq[AnomalyPipeline] = {

    val detectionModel = new Categorical.Detection[T](refAggrFunc, statisticalFunc, anomalyEvaluationFunc)
    val aggregationFactory = new Categorical.AggregationFactory[T]
    buildModel(detectionModel, aggregationFactory)
  }

  def buildNumericalModel[T: Numeric, K, U](refAggrFunc: Seq[StatisticsContainer[T]] => K,
    statisticalFunc: (K, T) => U,
    anomalyEvaluationFunc: U => Boolean,
    maxWindowCapacity: Int = Numerical.DEFAULT_CAPACITY,
    containerType: ContainerType = Numerical.DEFAULT_CONTAINER_TYPE): Seq[AnomalyPipeline] = {

    val detectionModel = new Numerical.Detection(refAggrFunc, statisticalFunc, anomalyEvaluationFunc)
    val aggregationFactory = new Numerical.AggregationFactory(containerType, Some(maxWindowCapacity))
    buildModel(detectionModel, aggregationFactory)
  }

  def buildDensityClusteringModel[T, U](
    clusteringFunc: (Seq[Array[Double]], Seq[Seq[Array[Double]]]) => Seq[Array[Double]],
    maxWindowCapacity: Int = DensityClustering.DEFAULT_CAPACITY,
    containerType: ContainerType = DensityClustering.DEFAULT_CONTAINER_TYPE): Seq[AnomalyPipeline] = {

    val detectionModel = new DensityClustering.Detection(clusteringFunc)
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
