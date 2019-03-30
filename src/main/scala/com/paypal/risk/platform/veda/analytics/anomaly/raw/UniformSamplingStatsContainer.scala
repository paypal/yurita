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
package com.paypal.risk.platform.veda.analytics.anomaly.raw

import com.paypal.risk.platform.veda.analytics.anomaly.statistical.Functions

/** allows to efficiently calculate and cache statistics on the extended container */
private[anomaly] class UniformSamplingStatsContainer[T: Numeric](maxCapacity: Int)
  extends UniformSamplingContainer[T](maxCapacity) with StatisticsContainer[T] {

  /**
   * return how many updates have been done to the values that the statistics are calculated on,
   * this mark will be used to determine if the cached calculation are stale.
   */
  override protected def getUpdatesCount: Int = getTotalUpdatesCount

  /**
   * Returns the arithmetic mean of the values in the snapshot.
   *
   * @return the arithmetic mean
   */
  def calculateMean: Double = Functions.Numerical.mean(getValues)

  /**
   * Returns the standard deviation of the values in the snapshot.
   *
   * @return the standard value
   */
  def calculateStdDev: Double = Functions.Numerical.stdev(getValues, getMean)
}

