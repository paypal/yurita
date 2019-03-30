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

import org.scalatest.{FlatSpec, Matchers}

import scala.collection.mutable

class BlockListTest extends FlatSpec with Matchers {

  behavior of "BlockListTest"

  it should "contain all elements using only 1 block" in {
    val list = new BlockList[Double]()
    val numElements = list.DEFAULT_BLOCK_SIZE - 1
    appendNElementsToBlockList(list, numElements)
    assertBlockListContent(list, numElements)
  }


  it should "contain all elements in 3 blocks" in {
    val list = new BlockList[Double]()
    val numElements = 3 * list.DEFAULT_BLOCK_SIZE
    appendNElementsToBlockList(list, numElements)
    assertBlockListContent(list, numElements)
  }

  it should "have correct size" in {
    val list = new BlockList[Double]()
    val numElements = 6000
    appendNElementsToBlockList(list, numElements)
    list.size shouldBe 6000
  }

  it should "have correct apply function" in {
    val list = new BlockList[Double]()
    val numElements = 6000
    appendNElementsToBlockList(list, numElements)
    list(numElements - 1) shouldBe numElements.toDouble
  }

  private def appendNElementsToBlockList(list: BlockList[Double], numElements: Int): Unit = {
    (1.0 to numElements.toDouble by 1.0).view.foreach { n =>
      list.append(n)
    }
  }

  private def assertBlockListContent(list: Iterable[Double], numElements: Int): Unit = {
    val itr = (1.0 to numElements.toDouble by 1.0).view.iterator
    list.foreach { n =>
      n shouldBe itr.next
    }
  }

  it should "have a kickass benchmark (append)" ignore {

    val limit = 100000000
    var blockListDiff = 0L
    (1 to 3).foreach { _ =>
      val blockList = new BlockList[Double]()
      val start = System.currentTimeMillis()
      appendNElementsToBlockList(blockList, limit)
      blockListDiff = System.currentTimeMillis() - start
    }

    var scalaListDiff = 0L
    (1 to 3).foreach { _ =>
      val scalaList = mutable.Buffer[Double]()
      val start = System.currentTimeMillis()
      (1.0 to limit.toDouble by 1.0).view.foreach { n =>
        scalaList.append(n)
      }
      scalaListDiff = System.currentTimeMillis() - start
    }

    println(s"blockList result: $blockListDiff")
    println(s"scalaList result: $scalaListDiff")
  }


  it should "have a kickass benchmark (scan)" ignore {
    val limit = 10000000

    val blockList = new BlockList[Double]()
    appendNElementsToBlockList(blockList, limit)

    val start1 = System.currentTimeMillis()
    assertBlockListContent(blockList, limit)
    val blockListDiff = System.currentTimeMillis() - start1


    val scalaList = mutable.Buffer[Double]()
    (1.0 to limit.toDouble by 1.0).view.foreach { n =>
      scalaList.append(n)
    }

    val start2 = System.currentTimeMillis()
    assertBlockListContent(scalaList, limit)
    val scalaListDiff = System.currentTimeMillis() - start2

    println(s"blockList result: $blockListDiff")
    println(s"scalaList result: $scalaListDiff")
  }

}
