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


import scala.collection.mutable

/**
 * copied from import com.codahale.metrics.Snapshot with some small modifications:
 * - result caching
 * - generics
 *
 * extends containers with integrated statistics capabilities
 *
 * @tparam T
 */
trait StatisticsContainer[T] extends BaseContainer[T] {

  private val MEAN = "mean"
  private val STDEV = "stdev"

  private var cacheGeneration = 0
  private val cache = mutable.Map[Any, Double]()

  /**
   * return how many updates have been done to the values that the statistics are calculated on,
   * this mark will be used to determine if the cached calculation are stale.
   */
  protected def getUpdatesCount: Int

  /**
   * Returns the value at the given quantile, uses a cache for efficiency.
   *
   * @param quantile a given quantile, in { @code [0..1]}
   * @return the value in the distribution at { @code quantile}
   */
  final def getQuantile(quantile: Double): Double = {
    getOrUpdate(quantile, calculateQuantile(quantile))
  }

  private def getOrUpdate(cacheKey: Any, newValue: => Double) = {
    if (cacheGeneration != getUpdatesCount) {
      cache.clear
      cacheGeneration = getUpdatesCount
    }
    cache.getOrElseUpdate(cacheKey, newValue)
  }

  /**
   * Returns the value at the given quantile.
   *
   * @param quantile a given quantile, in { @code [0..1]}
   * @return the value in the distribution at { @code quantile}
   */
  protected def calculateQuantile(quantile: Double): Double = throw new UnsupportedOperationException

  /**
   * Returns the median value in the distribution.
   *
   * @return the median value
   */
  def getMedian: Double = getQuantile(0.5)

  /**
   * Returns the value at the 75th percentile in the distribution.
   *
   * @return the value at the 75th percentile
   */
  def get75thPercentile: Double = getQuantile(0.75)

  /**
   * Returns the value at the 95th percentile in the distribution.
   *
   * @return the value at the 95th percentile
   */
  def get95thPercentile: Double = getQuantile(0.95)

  /**
   * Returns the value at the 98th percentile in the distribution.
   *
   * @return the value at the 98th percentile
   */
  def get98thPercentile: Double = getQuantile(0.98)

  /**
   * Returns the value at the 99th percentile in the distribution.
   *
   * @return the value at the 99th percentile
   */
  def get99thPercentile: Double = getQuantile(0.99)

  /**
   * Returns the value at the 99.9th percentile in the distribution.
   *
   * @return the value at the 99.9th percentile
   */
  def get999thPercentile: Double = getQuantile(0.999)

  /**
   * Returns the highest value in the snapshot.
   *
   * @return the highest value
   */
  def getMax: T = throw new UnsupportedOperationException

  /**
   * Returns the arithmetic mean of the values in the snapshot.
   *
   * @return the arithmetic mean
   */
  protected def calculateMean: Double

  /**
   * Returns the arithmetic mean of the values in the snapshot.
   *
   * @return the arithmetic mean
   */
  final def getMean: Double = {
    getOrUpdate(MEAN, calculateMean)
  }


  /**
   * Returns the lowest value in the snapshot.
   *
   * @return the lowest value
   */
  def getMin: T = throw new UnsupportedOperationException

  /**
   * Returns the standard deviation of the values in the snapshot.
   *
   * @return the standard value
   */
  protected def calculateStdDev: Double

  /**
   * Returns the standard deviation of the values in the snapshot.
   *
   * @return the standard value
   */
  final def getStdDev: Double = {
    getOrUpdate(STDEV, calculateStdDev)
  }

}
