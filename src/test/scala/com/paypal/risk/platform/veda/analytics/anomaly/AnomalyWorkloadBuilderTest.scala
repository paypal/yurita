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

import org.scalatest.{FlatSpec, Matchers}

class AnomalyWorkloadBuilderTest extends FlatSpec with Matchers {
  private val TS = "timestamp"
  private val DURATION = "10 seconds"

  "build method" should "fail for 0 pipelines" in {
    val builder = AnomalyWorkload.builder()
      .addAllPipelines(Seq.empty)

    an[IllegalArgumentException] should be thrownBy builder.buildWithWatermark(TS, DURATION)
  }
}
