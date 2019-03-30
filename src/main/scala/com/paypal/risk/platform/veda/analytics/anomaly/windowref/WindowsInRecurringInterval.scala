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
 * allows to reference all windows inside a specified time interval which will repeat going back in time
 * every sliding interval inside a total period.
 * e.g. 16:00 ~ 17:00 sliding back every 24 hours over the last 3 months period
 *
 * note: keys range edges are inclusive [start, end]
 * */
private class WindowsInRecurringInterval(intervalStart: Timestamp, intervalEnd: Timestamp,
  slideBackEvery: String, totalPeriod: String) extends WindowRefStrategy {

  require(intervalEnd.getTime > intervalStart.getTime, "referencing interval must be positive")
  private val startMilli = intervalStart.getTime
  private val endMilli = intervalEnd.getTime

  private val slideMilli = Utils.durationToMillis(slideBackEvery)

  private val periodMilli = Utils.durationToMillis(totalPeriod)

  override def getReferenceWindowKeysFunc(window: WindowStrategy): String => Seq[String] =
    recurringWindows(window)

  private def recurringWindows(window: WindowStrategy)(windowKey: WindowKey): Seq[String] = {
    require(windowKey.forall(_.isDigit),
      s"window key: $windowKey is not compatible with decimal window reference")

    val refKeysSeq = for {
      start <- startMilli to (startMilli - periodMilli) by -slideMilli
      end <- endMilli to (endMilli - periodMilli) by -slideMilli
    } yield {
      val startKey = window.extractTimeWindowKeys(new Timestamp(start)).map(_.toLong).min
      val endKey = window.extractTimeWindowKeys(new Timestamp(end)).map(_.toLong).max
      (startKey to endKey).map(_.toString)
    }
    refKeysSeq.flatten.distinct
  }
}
