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

class WindowsInDurationBeforeTest extends FlatSpec with Matchers {

  behavior of "WindowsInDurationBefore"

  it should "return keys of windows in duration before current key" in {
    val ref = new WindowsInDurationBefore("10 seconds")
    val refFunc = ref.getReferenceWindowKeysFunc(Window.fixed("1 second"))
    // for a milli timestamp 444666 with interval of 1 sec key is 444, 10 sec before is 434666 with key 434
    val expected = (434 until 444).map(_.toString)
    refFunc("444") shouldBe expected
  }
}
