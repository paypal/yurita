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
package com.paypal.risk.platform.veda.analytics.anomaly.window

import java.sql.Timestamp

import org.scalatest.{FlatSpec, Matchers}

class GlobalWindowTest extends FlatSpec with Matchers {

  behavior of "GlobalWindow"

  it should "always return the same window key" in {
    val window = GlobalWindow
    val key1 = window.extractTimeWindowKeys(new Timestamp(System.currentTimeMillis()))
    val key2 = window.extractTimeWindowKeys(new Timestamp(1))
    key1 shouldBe key2
  }

  it should "not have a timeout timestamp" in {
    val window = GlobalWindow
    val key1 = window.extractTimeWindowKeys(new Timestamp(1)).head
    val timeout = window.getTimeoutTimestamp(key1)
    timeout shouldBe None
  }

}
