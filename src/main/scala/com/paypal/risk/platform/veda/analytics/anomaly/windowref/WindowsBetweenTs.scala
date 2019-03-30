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

import com.paypal.risk.platform.veda.analytics.anomaly.WindowKey
import com.paypal.risk.platform.veda.analytics.anomaly.window.WindowStrategy

/** note: keys range edges are inclusive [start, end] */
private class WindowsBetweenTs(start: Timestamp, end: Timestamp) extends WindowRefStrategy {
  require(end.getTime > start.getTime, "referencing interval must be positive")

  override def getReferenceWindowKeysFunc(window: WindowStrategy): String => Seq[String] =
    timestampWindowDiff(window)

  private def timestampWindowDiff(window: WindowStrategy)(windowKey: WindowKey): Seq[String] = {
    require(windowKey.forall(_.isDigit),
      s"window key: $windowKey is not compatible with decimal window reference")
    val startKey = window.extractTimeWindowKeys(start).map(_.toLong).min
    val endKey = window.extractTimeWindowKeys(end).map(_.toLong).max
    (startKey to endKey).map(_.toString)
  }
}
