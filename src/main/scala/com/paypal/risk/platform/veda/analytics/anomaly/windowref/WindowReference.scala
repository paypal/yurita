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

object WindowReference {

  /** no reference */
  def NOREF: WindowRefStrategy = NoRef

  // reference a single window when detecting anomalies
  object SingleRef {

    /** reference the previous window if ref window was not empty */
    def lastWindow: WindowRefStrategy = new NthWindowBeforeCurrent(1)

    /** for window i reference previous window i - n if ref window was not empty */
    def previousNthWindow(n: Long): WindowRefStrategy = new NthWindowBeforeCurrent(n)

    /** window will reference the window of duration (e.g. "1 hour") time before current */
    def singleWindowBeforeDuration(duration: String): WindowRefStrategy = new SingleWindowBeforeDuration(duration)
  }

  // reference multiple windows when detecting anomalies
  object MultiRef {

    /** window will reference all windows in duration (e.g. "1 hour") time before current */
    def allWindowsInLastDuration(duration: String): WindowRefStrategy = new WindowsInDurationBefore(duration)

    /** window will reference n windows before current */
    def lastNWindows(n: Long): WindowRefStrategy = new LastNWindows(n)

    /**
     * window will reference all windows in duration (e.g. "1 hour") time before current,
     * sliding back 24 hours over a total period (e.g. "1 week")
     */
    def dailyWindows(durationBeforeCurrent: String, totalPeriod: String): WindowRefStrategy =
      recurringDuration(durationBeforeCurrent, "24 hours", totalPeriod)

    /** window will reference all windows between specified timestamps (inclusively) */
    def windowsBetweenTs(startTs: Timestamp, endTs: Timestamp): WindowRefStrategy = new WindowsBetweenTs(startTs, endTs)

    /**
     * window will reference all windows between specified timestamps (inclusively)
     * sliding back every duration time (e.g. "24 hours") over a period (e.g. "1 week")
     */
    def recurringInterval(intervalStart: Timestamp, intervalEnd: Timestamp, slideBackEvery: String,
      totalPeriod: String): WindowRefStrategy =
      new WindowsInRecurringInterval(intervalStart, intervalEnd, slideBackEvery, totalPeriod)

    /**
     * window will reference all windows in duration (e.g. "1 hour")
     * sliding back every duration time (e.g. "24 hours") over a total period (e.g. "1 week")
     */
    def recurringDuration(durationBeforeCurrent: String, slideBackEvery: String, totalPeriod: String)
    : WindowRefStrategy =
      new WindowsInRecurringDuration(durationBeforeCurrent, slideBackEvery, totalPeriod)
  }
}
