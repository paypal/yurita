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

import java.sql.Timestamp

import com.paypal.risk.platform.veda.analytics.anomaly.window.Window
import org.scalatest.{FlatSpec, Matchers}

class WindowsBetweenTsTest extends FlatSpec with Matchers {

  behavior of "WindowsBetweenTs"

  it should "return keys of windows between 2 timestamps" in {
    val start = new Timestamp(10000)
    val end = new Timestamp(20000)
    val ref = new WindowsBetweenTs(start, end)
    val refFunc = ref.getReferenceWindowKeysFunc(Window.fixed("1 second"))
    // for any key we will get the same keys in ts interval
    val expected = (10 to 20).map(_.toString)
    refFunc("444") shouldBe expected
  }

}
