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

import com.paypal.risk.platform.veda.analytics.anomaly.window.WindowStrategy
import com.paypal.risk.platform.veda.analytics.anomaly.windowref.WindowRefStrategy

/**
 * an container class for defining anomaly detection pipeline components,
 *
 * @param colKey         name of the column to be tracked by this pipeline
 * @param windowStrategy defines how to window the data stream
 * @param windowRefStrategy defines which windows a the detection model will use as reference
 * @param aggrFactory  used for creating new data windows
 * @param anomalyModel   model for deciding when data in a window is considered an anomaly
 * @param id             The ID of the pipeline
 */
case class AnomalyPipeline(colKey: String,
  windowStrategy: WindowStrategy,
  windowRefStrategy: WindowRefStrategy,
  aggrFactory: DataAggregationFactory,
  anomalyModel: DetectionModel,
  id: String = UUID.randomUUID().toString) {
}
