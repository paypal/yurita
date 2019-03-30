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

import com.paypal.risk.platform.veda.analytics.anomaly.WindowKey
import com.paypal.risk.platform.veda.analytics.anomaly.window.WindowStrategy

private class NthWindowBeforeCurrent(n: Long) extends WindowRefStrategy {
  require(n > 0, "referencing previous window must be positive")

  override def getReferenceWindowKeysFunc(window: WindowStrategy): String => Seq[String] =
    decimalWindowDiff

  private def decimalWindowDiff(windowKey: WindowKey): Seq[String] = {
    require(windowKey.forall(_.isDigit),
      s"window key: $windowKey is not compatible with decimal window reference")
    val refWindowKey = windowKey.toLong - n
    Seq(refWindowKey.toString)
  }
}
