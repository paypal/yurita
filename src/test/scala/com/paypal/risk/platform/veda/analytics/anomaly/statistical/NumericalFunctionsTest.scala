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

import com.paypal.risk.platform.veda.analytics.anomaly.raw.{RawStatsContainer, StatisticsContainer}
import org.scalatest.{FlatSpec, Matchers}

class NumericalFunctionsTest extends FlatSpec with Matchers {

  private def statsContainer(seq: Seq[Double]): StatisticsContainer[Double] = {
    val container = new RawStatsContainer[Double]
    seq.foreach(container.update)
    container
  }

  behavior of "Numerical mean method"

  it should "return the correct mean" in {
    val seq = Seq(1.0, 9.0)
    Functions.Numerical.mean(seq) shouldBe 5.0
  }

  behavior of "Numerical stdev method"

  it should "return the the correct stdev" in {
    val seq = Seq(1.0, 9.0)
    Functions.Numerical.stdev(seq, 5.0) shouldBe 4.0
  }


  behavior of "Numerical avgMeanAndStdevRef method"

  it should "return an empty option if there was no reference" in {
    val seq = Seq()
    Functions.Numerical.avgMeanAndStdevRef(seq) shouldBe None
  }

  it should "return the mean and stdev of only 1 seq" in {
    val container = statsContainer(Seq(1.0, 9.0))
    Functions.Numerical.avgMeanAndStdevRef(Seq(container)) shouldBe Some((5.0, 4.0))
  }

  it should "return the mean and stdev of 2 seq" in {
    val container1 = statsContainer(Seq(1.0, 9.0))
    val container2 = statsContainer(Seq(3.0, 4.0))
    Functions.Numerical.avgMeanAndStdevRef(Seq(container1, container2)) shouldBe Some((4.25, 2.25))
  }


  behavior of "Numerical zScore method"

  it should "return the correct zScore" in {
    val mean = 5.0
    val stdev = 4.0
    val point = 1.0
    val meanAndStdev = Some((mean, stdev))
    Functions.Numerical.zScore(meanAndStdev, point) shouldBe 1.0
  }

  it should "return 0 if no mean and stdev were specified" in {
    val meanAndStdev = None
    val point = 1.0
    Functions.Numerical.zScore(meanAndStdev, point) shouldBe 0.0
  }

  it should "return 0 if stdev is zero and given value is equal to mean" in {
    val mean = 5.0
    val stdev = 0.0
    val point = 5.0
    val meanAndStdev = Some((mean, stdev))
    Functions.Numerical.zScore(meanAndStdev, point) shouldBe 0.0
  }

  it should "return max double if stdev is zero and given value is not equal to mean" in {
    val mean = 5.0
    val stdev = 0.0
    val point = 1.0
    val meanAndStdev = Some((mean, stdev))
    Functions.Numerical.zScore(meanAndStdev, point) shouldBe Double.MaxValue
  }
}
