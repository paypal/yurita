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

import scala.collection.mutable

class AnomalyWorkloadBuilder() extends Serializable {
  /** pipelines to be included in workload, must have at least 1 */
  private val pipelines = mutable.ArrayBuffer[AnomalyPipeline]()

  /** partitioners (optional) are set to partition the data of a specific column to deal with large key spaces */
  private val partitioners = mutable.ArrayBuffer[ColumnPartitioner]()

  /** add an anomaly detection pipeline to workload */
  def addPipeline(pipeline: AnomalyPipeline): AnomalyWorkloadBuilder = {
    pipelines += pipeline
    this
  }

  /** add multiple anomaly detection pipelines to workload */
  def addAllPipelines(pipelineSeq: Seq[AnomalyPipeline]): AnomalyWorkloadBuilder = {
    pipelines ++= pipelineSeq
    this
  }

  /**
   * add partitioner with default implementation for a specific column
   * @param colName column name of a large keyspace that its values need to be partitioned
   * @param shards number of shards for the default partitioner (optional, default value = 10)
   * @return
   */
  def addPartitioner(colName: String, shards: Int = 10): AnomalyWorkloadBuilder = {
    addPartitioner(colName, shards, (s: Any) => Math.abs(s.hashCode % shards))
  }

  /**
   * add a partitioner for a specific column to workload
   * @param colName column name of a large keyspace that its values need to be partitioned
   * @param shards number of shards for this partition
   * @param partitionerFunc partitioner function
   * @return
   */
  def addPartitioner(colName: String, shards: Int, partitionerFunc: Any => Int): AnomalyWorkloadBuilder = {
    partitioners += ColumnPartitioner(colName, shards, partitionerFunc)
    this
  }

  /**
   * check creation requirements and merge user pipelines to a single efficient processing flow
   *
   * @param timestampField column name of the event time timestamp
   * @param watermark      duration to keep state for late events
   * @return anomaly detection workload
   */
  def buildWithWatermark(timestampField: String, watermark: String): AnomalyWorkload = {
    require(pipelines.nonEmpty, "in order to run anomaly detection you must specify at least 1 pipeline")
    AnomalyWorkload(pipelines, timestampField, watermark, partitioners)
  }

}

