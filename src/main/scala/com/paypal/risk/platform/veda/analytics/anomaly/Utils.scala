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
package com.paypal.risk.platform.veda.analytics.anomaly

import org.apache.spark.unsafe.types.CalendarInterval

import scala.collection.mutable

object Utils {
  def mapToString[T, U](map: Map[T, U], title: String): String = {
    val stringBuilder = new mutable.StringBuilder()
    stringBuilder.append(s"\n\n $title \n")
    stringBuilder.append(s"+-----------------------+\n")
    val mapStr = map.toSeq
      .map { case (k, v) => s"$k : $v" }
      .mkString("\n")
    stringBuilder.append(mapStr)
    stringBuilder.append(s"\n+-----------------------+\n\n")
    stringBuilder.toString()
  }

  def refTimeRangesString(report: Report): String =
    s"""
      | Current window time range: ${report.getCurrentTimeRange}
      | Reference Is Based On ${report.getRefTimeRanges.size} Windows:
      | ${report.getRefTimeRanges.mkString("\n")}
      |\n""".stripMargin


  def durationToMillis(duration: String): Long = {
    val interval = CalendarInterval.fromString(s"interval $duration")
    require(Option(interval).isDefined, s"$duration is an invalid time interval, check for spelling error")
    interval.milliseconds
  }
}
