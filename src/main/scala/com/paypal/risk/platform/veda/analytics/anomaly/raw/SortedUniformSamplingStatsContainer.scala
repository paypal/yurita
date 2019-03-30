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

/** allows to efficiently calculate and cache statistics on the extended container - supports quntiles */
private[anomaly] class SortedUniformSamplingStatsContainer[T](maxCapacity: Int)
  extends SortedUniformSamplingContainer[T](maxCapacity) with StatisticsContainer[T] {


  /**
   * return how many updates have been done to the values that the statistics are calculated on,
   * this mark will be used to determine if the cached calculation are stale.
   */
  override protected def getUpdatesCount: Int = getTotalUpdatesCount

  /**
   * Returns the value at the given quantile.
   *
   * @param quantile a given quantile, in { @code [0..1]}
   * @return the value in the distribution at { @code quantile}
   */
  override def calculateQuantile(quantile: Double): Double = snapshot.getValue(quantile)

  /**
   * Returns the highest value in the snapshot.
   *
   * @return the highest value
   */
  override def getMax: T = snapshot.getMax.asInstanceOf[T]

  /**
   * Returns the arithmetic mean of the values in the snapshot.
   *
   * @return the arithmetic mean
   */
  def calculateMean: Double = snapshot.getMean

  /**
   * Returns the lowest value in the snapshot.
   *
   * @return the lowest value
   */
  override def getMin: T = snapshot.getMin.asInstanceOf[T]

  /**
   * Returns the standard deviation of the values in the snapshot.
   *
   * @return the standard value
   */
  def calculateStdDev: Double = snapshot.getStdDev
}
