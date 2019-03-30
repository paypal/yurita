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
package com.paypal.risk.platform.veda.analytics.anomaly.statistical

import com.paypal.risk.platform.veda.analytics.anomaly.statistical.Functions.StatisticalResult
import org.scalatest.{FlatSpec, Matchers}

class CategoricalFunctionsTest extends FlatSpec with Matchers {

  behavior of "Categorical avgRef method"

  it should "return the same Map if there's only one Map" in {
    val someMap = Map("A" -> 1L)
    Functions.Categorical.avgRef[String](Seq(someMap)) shouldEqual someMap
  }

  it should "return an Empty Map if no refs are provided" in {
    Functions.Categorical.avgRef[String](Seq.empty) shouldEqual Map.empty
  }

  it should "return an Average of the given maps" in {
    val a1 = Map("A" -> 10L)
    val a2 = Map("A" -> 20L)
    val expected = Map("A" -> 15)
    Functions.Categorical.avgRef[String](Seq(a1, a2)) shouldEqual expected
  }

  it should "return an Average of the given maps, even when some keys are missing" in {
    val a1 = Map("A" -> 10L)
    val a2 = Map("A" -> 20L, "B" -> 20L)
    val expected = Map("A" -> 15, "B" -> 10L)
    Functions.Categorical.avgRef[String](Seq(a1, a2)) shouldEqual expected
  }

  behavior of "Categorical entropy method"

  private val emptyMap = Map.empty[String, Long]

  private def inputMap(key: String) = Map(key -> 10L)

  private def outputMap(key: String) = Map(key -> 0.0)

  it should "return 0.0 contribution in case one Map is missing" in {
    Functions.Categorical.entropy(inputMap("A"), emptyMap) shouldEqual StatisticalResult(0.0, outputMap("A"))
    Functions.Categorical.entropy(emptyMap, inputMap("A")) shouldEqual StatisticalResult(0.0, outputMap("A"))
  }

  it should "return 0.0 contribution to entropy on similar maps" in {
    Functions.Categorical.entropy(inputMap("A"), inputMap("A")) shouldEqual StatisticalResult(0.0, outputMap("A"))
  }

  it should "return 0.0 contribution to entropy on disjoint maps" in {
    Functions.Categorical.entropy(inputMap("A"), inputMap("B")) shouldEqual
      StatisticalResult(0.0, outputMap("A") ++ outputMap("B"))
  }

  it should "return EntropyResult with 0.0 entropy on same ratio maps" in {
    val someMap = Map("A" -> 10L, "B" -> 10L)
    Functions.Categorical.entropy(someMap, someMap.mapValues(_ * 2)) shouldEqual
      StatisticalResult(0.0, Map("A" -> 0.0, "B" -> 0.0))
  }

}
