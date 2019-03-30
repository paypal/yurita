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

class SlidingWindowTest extends FlatSpec with Matchers {

  behavior of "SlidingWindow"

  it should "correct window keys" in {
    val window = SlidingWindow("4 seconds", "2 seconds")
    val keys = window.extractTimeWindowKeys(new Timestamp(9500))
    keys shouldBe Seq("3", "4")
  }

  it should "have correct timeout timestamp" in {
    val window = SlidingWindow("4 seconds", "2 seconds")
    val key1 = window.extractTimeWindowKeys(new Timestamp(9500)).head
    val timeout = window.getTimeoutTimestamp(key1).get
    timeout shouldBe 10000L
  }

  it should "correct window keys for edge timestamp" in {
    val window = SlidingWindow("4 seconds", "2 seconds")
    val keys = window.extractTimeWindowKeys(new Timestamp(10000))
    keys shouldBe Seq("4", "5")
  }

  it should "have correct timeout timestamp for edge timestamp" in {
    val window = SlidingWindow("4 seconds", "2 seconds")
    val key1 = window.extractTimeWindowKeys(new Timestamp(10000)).head
    val timeout = window.getTimeoutTimestamp(key1).get
    timeout shouldBe 12000L
  }

  it should "have correct start timestamp" in {
    val window = SlidingWindow("4 seconds", "2 seconds")
    val key1 = window.extractTimeWindowKeys(new Timestamp(9500)).head
    val timeout = window.getStartTimestamp(key1).get
    timeout shouldBe 6000L
  }
}
