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

import org.apache.spark.unsafe.types.CalendarInterval

private case class SlidingWindow(duration: String, slideEvery: String) extends WindowStrategy {
  private val durationIntervalMillis = getDurationIntervalMillis
  private val slidingIntervalMillis = getSlidingIntervalMillis

  /** extract from a timestamp its relevant window keys for grouping */
  override def extractTimeWindowKeys: Timestamp => Seq[String] = getSlidingIntervalKeys

  /** for a given window key return a timestamp in milliseconds specifying when this window ends */
  override def getTimeoutTimestamp: String => Option[Long] = getDurationIntervalLimit


  /** for a given window key return a timestamp in milliseconds specifying when this window start.
   * this is optional and used mostly for more logging
   *
   */
  override def getStartTimestamp: String => Option[Long] = getIntervalStart

  /**
   *  for a timestamp:
   *  1. lower bound - reduce from timestamp the duration to get the first timestamp that is not in the group, adding
   *  the slide interval will give us a timestamp in the first window
   *  2. upper bound - the original timestamp
   *  3. we convert every timestamp to a key by its sliding window start position
   */
  private def getSlidingIntervalKeys(ts: Timestamp): Seq[String] = {
    val lowerBound = ts.getTime - durationIntervalMillis + slidingIntervalMillis
    val upperBound = ts.getTime
    (lowerBound to upperBound by slidingIntervalMillis).map{ ts => getFixedIntervalKey(ts) }.distinct
  }

  private def getFixedIntervalKey(ts: Long): String = (ts / slidingIntervalMillis).toString

  private def getDurationIntervalMillis: Long = {
    val interval = CalendarInterval.fromString(s"interval $duration")
    require(interval != null, s"$duration is an invalid time interval, check for spelling error")
    interval.milliseconds
  }

  private def getSlidingIntervalMillis: Long = {
    val interval = CalendarInterval.fromString(s"interval $slideEvery")
    require(interval != null, s"$slideEvery is an invalid time interval, check for spelling error")
    interval.milliseconds
  }

  private def getDurationIntervalLimit(windowKey: String): Option[Long] = {
    val timeout = (windowKey.toLong * slidingIntervalMillis) + durationIntervalMillis
    Option(timeout)
  }

  private def getIntervalStart(windowKey: String): Option[Long] = {
    val timeout = windowKey.toLong * slidingIntervalMillis
    Option(timeout)
  }
}
