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
 * an efficient implementation of a large scale collection, meant to hold many data point:
 * dynamic array of fixed sized blocks, design goals:
 *  - no data copy when capacity increases
 *  - fast appends and full scan
 *  - no concurrency (should be modified only by a single thread at a time)
 *
 *
 */
class BlockList[T] extends Seq[T] {
  // 32kb chunks for Double values
  private[anomaly] val DEFAULT_BLOCK_SIZE = 4000

  private val blocks = mutable.Buffer[Array[Any]]()
  blocks.append(new Array[Any](DEFAULT_BLOCK_SIZE))
  private var currentBlock = 0
  private var currentIndex = 0

  private final def get(block: Int, index: Int): T = blocks(block)(index).asInstanceOf[T]

  override def size: Int = currentBlock * DEFAULT_BLOCK_SIZE + currentIndex

  def append(elem: T): Unit = {
    blocks(currentBlock).update(currentIndex, elem)
    currentIndex += 1
    if (currentIndex == DEFAULT_BLOCK_SIZE) {
      blocks.append(new Array[Any](DEFAULT_BLOCK_SIZE))
      currentBlock += 1
      currentIndex = 0
    }
  }

  def updateAt(pos: Int, elem: T): Unit = {
    blocks(pos / DEFAULT_BLOCK_SIZE).update(pos % DEFAULT_BLOCK_SIZE, elem)

  }


  override def iterator: Iterator[T] = new BlockListIterator(this, size)

  private class BlockListIterator(list: BlockList[T], size: Int) extends Iterator[T] {
    private var currentBlock = 0
    private var currentIndex = 0
    private var remaining = size

    override def hasNext: Boolean = remaining > 0

    override def next(): T = {
      val res = list.get(currentBlock, currentIndex)
      remaining -= 1
      currentIndex += 1
      if (currentIndex == DEFAULT_BLOCK_SIZE) {
        currentBlock += 1
        currentIndex = 0
      }
      res
    }
  }

  override def length: Int = size

  override def apply(idx: Int): T = get(idx / DEFAULT_BLOCK_SIZE, idx % DEFAULT_BLOCK_SIZE)
}
