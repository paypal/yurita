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

class FixedWindowTest extends FlatSpec with Matchers {

  behavior of "FixedWindow"

  it should "have same key for timestamps in interval" in {
    val window = FixedWindow("1 second")
    val key1 = window.extractTimeWindowKeys(new Timestamp(150))
    val key2 = window.extractTimeWindowKeys(new Timestamp(160))
    key1 shouldBe key2
  }

  it should "have different key for timestamps in different intervals" in {
    val window = FixedWindow("1 second")
    val key1 = window.extractTimeWindowKeys(new Timestamp(150))
    val key2 = window.extractTimeWindowKeys(new Timestamp(1150))
    key1 should not be key2
  }

  it should "have correct window key" in {
    val window = FixedWindow("1 second")
    val key1 = window.extractTimeWindowKeys(new Timestamp(444666)).head
    key1 shouldBe "444"
  }

  it should "have correct timeout timestamp" in {
    val window = FixedWindow("1 second")
    val key1 = window.extractTimeWindowKeys(new Timestamp(444666)).head
    val timeout = window.getTimeoutTimestamp(key1).get
    timeout shouldBe 445000L
  }

  it should "have correct start timestamp" in {
    val window = FixedWindow("1 second")
    val key1 = window.extractTimeWindowKeys(new Timestamp(444666)).head
    val timeout = window.getStartTimestamp(key1).get
    timeout shouldBe 444000L
  }

}
