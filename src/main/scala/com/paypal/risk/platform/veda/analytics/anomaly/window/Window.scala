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

object Window {

  /** all records will be collected in a single global window */
  def global: WindowStrategy = GlobalWindow

  /**
   * records will be collected into non overlapping fixed sized intervals
   *  e.g. "10 minutes", "1 hour"
   */
  def fixed(duration: String): WindowStrategy = FixedWindow(duration)

  /**
   * records will be collected into overlapping fixed sized intervals
   *  e.g. "10 minutes" with "5 minutes" slide, will have a 10 minutes window for every 5 minutes
   */
  def sliding(duration: String, slideEvery: String): WindowStrategy = SlidingWindow(duration, slideEvery)
}
