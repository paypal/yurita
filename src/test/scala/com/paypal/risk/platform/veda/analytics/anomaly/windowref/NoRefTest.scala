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
package com.paypal.risk.platform.veda.analytics.anomaly.windowref

import com.paypal.risk.platform.veda.analytics.anomaly.window.Window
import org.scalatest.{FlatSpec, Matchers}

class NoRefTest extends FlatSpec with Matchers {

  behavior of "NoRef"

  it should "have reference disabled" in {
    val ref = NoRef
    ref.isReferenceEnabled shouldBe false
  }

  it should "return no reference aggr" in {
    val ref = NoRef
    val refFunc = ref.getReferenceWindowKeysFunc(Window.fixed("1 hour"))
    refFunc("a") shouldBe Seq.empty
  }
}
