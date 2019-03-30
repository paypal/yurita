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

class WindowsInRecurringDurationTest extends FlatSpec with Matchers {
  behavior of "WindowsInRecurringDuration"

  // milliseconds
  val DAY = 86400000
  val SEC = 1000

  it should "return keys of windows in duration sliding 24 hours and limited by period and without current window" in {

    val ref = new WindowsInRecurringDuration("2 seconds", "24 hours",
      "24 hours")
    val refFunc = ref.getReferenceWindowKeysFunc(Window.fixed("1 second"))

    val expected = Seq(0, SEC, 2 * SEC, DAY, SEC + DAY).map(_ / SEC).map(_.toString)
    val windowKey = ((2 * SEC + DAY) / SEC).toString

    refFunc(windowKey).sorted shouldBe expected
  }
}
