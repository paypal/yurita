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

import scala.util.Random

/** stores data values in a stream with a uniform probability,
 * uses reservoir sampling algorithm,
 * stores all values until reaches maxCapacity, then apply sampling for every new point
 * */
private[anomaly] class UniformSamplingContainer[T](maxCapacity: Int)
  extends BaseContainer[T] {

  private val RANDOM_SEED = 5
  private val r = new Random(RANDOM_SEED)
  private val list = new BlockList[T]()


  /**
   * adds a new element to the container
   * using reservoir sampling algorithms to give a uniform distribution
   *
   * @param value the value to add
   */
  override def updateContainer(value: T): Unit = {
    if (size < maxCapacity) {
      list.append(value)
    } else {
      val pos = r.nextInt(getTotalUpdatesCount)
      if (pos < size) list.updateAt(pos, value)
    }
  }

  /**
   * Returns the entire set of values in the snapshot.
   *
   * @return the entire set of values
   */
  override def getValues: Seq[T] = list

  /**
   * Returns the number of values in the snapshot.
   *
   * @return the number of values
   */

  override def size: Int = list.size

}

