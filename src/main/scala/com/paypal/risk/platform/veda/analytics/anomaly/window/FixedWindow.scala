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

import com.paypal.risk.platform.veda.analytics.anomaly.Utils

private case class FixedWindow(duration: String) extends WindowStrategy {
  private val intervalMillis = Utils.durationToMillis(duration)

  override def extractTimeWindowKeys: Timestamp => Seq[String] = getFixedIntervalKey

  override def getTimeoutTimestamp: String => Option[Long] = getIntervalLimit

  override def getStartTimestamp: String => Option[Long] = getIntervalStart

  private def getFixedIntervalKey(ts: Timestamp): Seq[String] = Seq((ts.getTime / intervalMillis).toString)

  private def getIntervalLimit(windowKey: String): Option[Long] = Option((windowKey.toLong + 1) * intervalMillis)

  private def getIntervalStart(windowKey: String): Option[Long] = Option(windowKey.toLong * intervalMillis)
}
