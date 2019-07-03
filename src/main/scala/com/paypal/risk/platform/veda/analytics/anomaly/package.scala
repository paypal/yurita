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
package com.paypal.risk.platform.veda.analytics

import org.apache.spark.sql.{Encoder, Encoders, Row}

package object anomaly {

  implicit val ear: Encoder[Report] = Encoders.kryo[Report]
  implicit val etsar: Encoder[(ColumnKey, Report, WindowRange)] = Encoders.kryo[(ColumnKey, Report, WindowRange)]
  implicit val eoar: Encoder[Option[Report]] = Encoders.kryo[Option[Report]]

  implicit val etm: Encoder[TrackedMetric] = Encoders.kryo[TrackedMetric]
  implicit val eitm: Encoder[InternalTrackedMetric] = Encoders.kryo[InternalTrackedMetric]
  implicit val ektm: Encoder[KeyedTrackedMetric] = Encoders.kryo[KeyedTrackedMetric]
  implicit val esktm: Encoder[Seq[KeyedTrackedMetric]] = Encoders.kryo[Seq[KeyedTrackedMetric]]


  implicit val edm: Encoder[DataAggregation] = Encoders.kryo[DataAggregation]
  implicit val ews: Encoder[WindowState] = Encoders.kryo[WindowState]

  implicit val eka: Encoder[KeyedAggregation] = Encoders.kryo[KeyedAggregation]
  implicit val eoka: Encoder[Option[KeyedAggregation]] = Encoders.kryo[Option[KeyedAggregation]]


  implicit val erow: Encoder[Row] = Encoders.kryo[Row]

  type PipelineKey = String
  type ShardKey = Int
  type AggregationKey = String
  type ColumnKey = String
  type WindowKey = String
  type WindowRange = String
}
