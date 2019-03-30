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

class NthWindowBeforeCurrentTest extends FlatSpec with Matchers {

  behavior of "NthWindowBeforeCurrent"

  it should "return the previous window Key" in {
    val ref = new NthWindowBeforeCurrent(1)
    val refFunc = ref.getReferenceWindowKeysFunc(Window.fixed("10 seconds"))
    refFunc("4446") shouldBe Seq("4445")
  }

  it should "return nth previous window key" in {
    val ref = new NthWindowBeforeCurrent(2)
    val refFunc = ref.getReferenceWindowKeysFunc(Window.fixed("10 seconds"))
    refFunc("4446") shouldBe Seq("4444")
  }
}
