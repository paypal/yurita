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

class WindowsInRecurringIntervalTest extends FlatSpec with Matchers {
  behavior of "WindowsInRecurringInterval"

  it should "return keys of windows between 2 timestamps with sliding and limited by period" in {
    val start = new Timestamp(100000)
    val end = new Timestamp(200000)
    val ref = new WindowsInRecurringInterval(start, end, "10 seconds", "20 seconds")
    val refFunc = ref.getReferenceWindowKeysFunc(Window.fixed("1 second"))

    val interval1 = (100 to 200).map(_.toString)
    val interval2 = (90 to 190).map(_.toString)
    val interval3 = (80 to 180).map(_.toString)
    val expected = (interval1 ++ interval2 ++ interval3).distinct

    // for any key we will get the same keys in ts interval
    refFunc("444") shouldBe expected
  }

}
