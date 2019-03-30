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

/**
 * allows to reference all windows at a duration before current window, which will slide back in time
 * inside a total period.
 * e.g. last 3 hours sliding back every 24 hours over the last 3 months period
 *
 * */
private class WindowsInRecurringDuration(durationBeforeCurrent: String, slideBackEvery: String, totalPeriod: String)
  extends WindowRefStrategy {

  private val durationMilli = Utils.durationToMillis(durationBeforeCurrent)

  private val periodMilli = Utils.durationToMillis(totalPeriod)

  private val slideBackEveryMilli = Utils.durationToMillis(slideBackEvery)

  override def getReferenceWindowKeysFunc(window: WindowStrategy): String => Seq[String] =
    recurringDurationWindows(window)

  private def recurringDurationWindows(window: WindowStrategy)(windowKey: WindowKey): Seq[String] = {
    require(windowKey.forall(_.isDigit),
      s"window key: $windowKey is not compatible with decimal window reference")

    val timeoutTs = window.getTimeoutTimestamp(windowKey)
    if (timeoutTs.isEmpty) return Seq.empty
    // since timeout is the first ts which is not in the window we reduce 1 to get a ts in the window
    val upperBoundTs = timeoutTs.get - 1

    val refKeys = (upperBoundTs to upperBoundTs - periodMilli by -slideBackEveryMilli).flatMap { ts =>

      val minKey = window.extractTimeWindowKeys(new Timestamp(ts - durationMilli)).map(_.toLong).min
      val maxKey = window.extractTimeWindowKeys(new Timestamp(ts)).map(_.toLong).max
      (minKey to maxKey).map(_.toString)
    }
    // we remove the current window from the reference
    refKeys.filter( _ != windowKey).distinct
  }
}
