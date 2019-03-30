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

object AnomalyTestImpl {

  class TestDataAggregationFactory extends DataAggregationFactory {
    override def createNewAggregation(): DataAggregation = new TestDataAggregation
  }

  class TestDataAggregation extends DataAggregation {
    var intCount = 0

    override def update(trackedMetric: TrackedMetric): Unit = {
      intCount += trackedMetric.value.asInstanceOf[Int]
    }
  }

  class TestDetectionModel extends DetectionModel {
    val THRESHOLD = 5

    override def detect(dataModel: DataAggregation, refDataaggr: Seq[DataAggregation]): Option[Report] = {
      val dm = dataModel.asInstanceOf[TestDataAggregation]
      val report = if (dm.intCount > THRESHOLD) TestReport(true) else TestReport(false)
      Option(report)
    }
  }

  case class TestReport(isAnomaly: Boolean) extends Report {
  }

}

