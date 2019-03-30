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

import com.paypal.risk.platform.veda.analytics.anomaly.Utils
import com.paypal.risk.platform.veda.analytics.anomaly.window.WindowStrategy

/** defines which window should be used for reference for each window in a pipeline */
trait WindowRefStrategy extends Serializable {
  private[anomaly] var watermarkDuration: Option[String] = None
  private[anomaly] var watermarkMilli: Option[Long] = None
  private[anomaly] var currentWindowIncluded: Boolean = false

  /** return a function that transform a windowKey to its reference window Keys if exists */
  def getReferenceWindowKeysFunc(window: WindowStrategy): String => Seq[String]

  /** marker that window should be stored as references for future window */
  def isReferenceEnabled: Boolean = true

  /**
   * set the duration to keep historical windows for future referencing, the windows are stored in memory and are
   * removed eventually after the TTL has expired
   */
  def withWatermark(duration: String): WindowRefStrategy = {
    watermarkMilli = Some(Utils.durationToMillis(duration))
    watermarkDuration = Some(duration)
    this
  }

  /** specify if the current window that is evaluated for anomaly should be included in the reference
   * in addition to the historical windows
   */
  def includeCurrentWindowInRef(include: Boolean = true): WindowRefStrategy = {
    currentWindowIncluded = include
    this
  }
}
