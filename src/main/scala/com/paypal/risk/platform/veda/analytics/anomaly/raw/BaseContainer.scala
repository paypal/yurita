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

/**
 * holds data values observed in a given window
 *
 * @tparam T
 */
trait BaseContainer[T] {

  private var totalCount: Int = 0

  /**
   * adds a new element to the container
   *
   * @param value the value to add
   */
  final def update(value: T): Unit = {
    totalCount += 1
    updateContainer(value)
  }

  /**
   * adds a new element to the container
   *
   * @param value the value to add
   */
  protected def updateContainer(value: T): Unit

  /**
   * @return the total value that were updated in this container,
   *         including those that are not currently in the container if there was sampling
   */
  def getTotalUpdatesCount: Int = totalCount

  /**
   * Returns the entire set of values in the snapshot.
   *
   * @return the entire set of values
   */
  def getValues: Seq[T]

  /**
   * Returns the number of values in the snapshot.
   *
   * @return the number of values
   */
  def size: Int
}

