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

import com.paypal.risk.platform.veda.analytics.anomaly.{Utils, WindowKey}
import com.paypal.risk.platform.veda.analytics.anomaly.window.WindowStrategy

private class WindowsInDurationBefore(duration: String) extends WindowRefStrategy {
  private val durationMilli = Utils.durationToMillis(duration)

  override def getReferenceWindowKeysFunc(window: WindowStrategy): String => Seq[String] =
    durationWindowDiff(window)

  /**
   *  for a given window key we want to know what was the key duration (e.g. 1 minute) time before,
   *  to calculate this we check the window strategy timeout timestamp in milliseconds.
   *  1. assuming this timestamp is the first timestamp of the next window,
   *  then decreasing it by 1 will give us a timestamp within the current window.
   *  2. now when subtracting the duration we will get a timestamp within a window which is duration time before.
   *  3. we extract the minimal window key which the new timestamp belongs to and that is the lower bound ref window
   */
  private def durationWindowDiff(window: WindowStrategy)(windowKey: WindowKey): Seq[String] = {
    require(windowKey.forall(_.isDigit),
      s"window key: $windowKey is not compatible with decimal window reference")

    val refKeys = window.getTimeoutTimestamp(windowKey)
      .map{ ts => ts - durationMilli - 1}
      .map(refTs => window.extractTimeWindowKeys(new Timestamp(refTs)))

    if (refKeys.isDefined) {
      val minWindowKey = refKeys.get.map(_.toLong).min
      (minWindowKey until windowKey.toLong).map(_.toString)
    } else Seq.empty

  }
}
