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

import com.codahale.metrics.{Snapshot, UniformReservoir}


/** stores data values, currently supports only long values, allows to uniformly sample and store in a sorted fashion */
private[anomaly] class SortedUniformSamplingContainer[T](maxCapacity: Int)
  extends BaseContainer[T] {

  private val reservoir = new UniformReservoir(maxCapacity)
  protected val snapshot: Snapshot = reservoir.getSnapshot

  /**
   * adds a new element to the container
   *
   * @param value the value to add
   */
  override def updateContainer(value: T): Unit = {
    reservoir.update(value.asInstanceOf[Long])
  }

  /**
   * Returns the entire set of values in the snapshot.
   *
   * @return the entire set of values
   */
  override def getValues: Seq[T] = snapshot.getValues.asInstanceOf[Seq[T]]

  /**
   * Returns the number of values in the snapshot.
   *
   * @return the number of values
   */
  override def size: Int = snapshot.size

}
