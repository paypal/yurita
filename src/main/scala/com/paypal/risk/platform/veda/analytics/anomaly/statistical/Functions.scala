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

import com.paypal.risk.platform.veda.analytics.anomaly.Utils
import com.paypal.risk.platform.veda.analytics.anomaly.clustering.{Clust4jHDBSCAN, VedaDbscanLib}
import com.paypal.risk.platform.veda.analytics.anomaly.raw.StatisticsContainer
import org.apache.commons.math3.util.FastMath

import scala.collection.JavaConverters._

object Functions {

  /**
   * result of a statistical comparison between aggregations
   *
   * @param result       the result of the statistical calculation
   * @param contributors the partial contribution of each element to the @result
   */
  case class StatisticalResult[T](result: Double, contributors: Map[T, Double]) {
    override def toString: String =
      s"""
        | Statistical Function Evaluated To: $result
        | ${Utils.mapToString(contributors, "each value's contribution to the result")}
      """.stripMargin
  }

  /**
   * Anomaly evaluation method that creates a detect anomaly method based on a given threshold
   */
  def simpleThreshold(threshold: Double)(value: Double): Boolean =
    value > threshold

  /**
   * Anomaly evaluation method that creates a detect anomaly method based on a given threshold
   */
  def statResultThreshold(threshold: Double)(value: StatisticalResult[_]): Boolean =
    value.result > threshold

  def statResultThresholdFunction[T](threshold: Double): Function1[StatisticalResult[T], Boolean] =
    Functions.statResultThreshold(threshold)

  object Categorical {

    /**
     * Returns the average of the given Maps
     */
    def avgRef[T](refMaps: Seq[Map[T, Long]]): Map[T, Long] = {
      if (refMaps.isEmpty) return Map.empty
      refMaps.map(_.toSeq)
        .reduce { (seq1, seq2) => seq1 ++ seq2 }
        .groupBy {
          _._1
        }
        .mapValues { seq =>
          seq.map {
            _._2
          }.sum
        }
        .mapValues { v => v / refMaps.size }
    }

    /**
     * Calculate entropy and the contribution of each value to the entropy calculation
     *
     * @param map1 values with their count in the current aggregation we are evaluating
     * @param map2 values with their count in the computed reference aggregation
     * @tparam T - Key types in the given Maps
     * @return StatisticalResult
     */
    def entropy[T](map1: Map[T, Long], map2: Map[T, Long]): StatisticalResult[T] = {

      if (map1.isEmpty || map2.isEmpty) {
        StatisticalResult(0.0, (map1 ++ map2).mapValues { _ => 0.0 })
      } else {
        val sum1 = map1.values.sum.toDouble
        val sum2 = map2.values.sum.toDouble
        val dist1 = map1.map { case (k, v) => (k, v / sum1) }
        val dist2 = map2.map { case (k, v) => (k, v / sum2) }

        val entropyContribution = dist1.map { case (t, p) =>
          val e = dist2.get(t).filter(_ > 0.0).map { q => p * math.log(p / q) }.getOrElse(0.0)
          t -> e
        }
        // add to the contributions the elements that only existed in the ref map
        val contributorsOnlyInMap2 = dist2.filter { value => !entropyContribution.contains(value._1) }
          .mapValues { _ => 0.0 }
        val allContributors = entropyContribution ++ contributorsOnlyInMap2

        StatisticalResult(entropyContribution.values.sum, allContributors)
      }
    }

    def avgRefFunction[T](): Function1[Seq[Map[T, Long]], Map[T, Long]] =
      avgRef

    def entropyFunction[T](): Function2[Map[T, Long], Map[T, Long], StatisticalResult[T]] =
      entropy
  }

  object Numerical {

    type Mean = Double
    type Stddev = Double

    /**
     * calculate the mean of mean of seq
     */
    def mean[T](seq: Iterable[T])(implicit numType: Numeric[T]): Double = {
      import numType._
      seq.sum.toDouble / seq.size
    }

    /**
     * calculate the stdev
     */
    def stdev[T](seq: Iterable[T], mean: Double)(implicit numType: Numeric[T]): Double = {
      import numType._
      val sqrDiffSum = seq.map { value =>
        val diff = value.toDouble - mean
        diff * diff
      }.sum

      FastMath.sqrt(sqrDiffSum / seq.size)
    }

    /**
     * calculate the mean of means and the average stdev across references
     */
    def avgMeanAndStdevRef[T](refContainers: Seq[StatisticsContainer[T]]): Option[(Mean, Stddev)] = {
      val refs = refContainers.filter(_.size > 0)
      if (refs.nonEmpty) {
        // calculate mean and std for each non-empty container and sum them
        val (meanSum, stdevSum) = refs.map { container =>
          (container.getMean, container.getStdDev)
        }
          .reduce { (a, b) => (a._1 + b._1, a._2 + b._2) }

        // return the average
        Some(meanSum / refs.size, stdevSum / refs.size)
      } else None
    }

    /**
     * calculate the positive zScore of a value compared to a reference mean and stdev,
     * empty option or stdev of 0 will return 0
     */
    def zScore[T](meanAndStdev: Option[(Mean, Stddev)], value: T)(implicit numType: Numeric[T]): Double = {
      import numType._
      meanAndStdev match {
        case Some((mean, 0.0)) => if (mean == value) 0.0 else Double.MaxValue
        case Some((mean, stdev)) => (value.toDouble - mean).abs / stdev
        case _ => 0.0
      }
    }

    def meanFunction[T]()(implicit numType: Numeric[T]): Function1[Iterable[T], Double] =
      mean

    def stdevFunction[T]()(implicit numType: Numeric[T]): Function2[Iterable[T], Double, Double] =
      stdev

    def avgMeanAndStdevRefFunction[T](): Function1[Seq[StatisticsContainer[T]], Option[(Mean, Stddev)]] =
      avgMeanAndStdevRef

  }

  object Clustering {
    /**
     * Clustering function should be used as a parameter to the density clustering detection model.
     * Applies an optimized version of the DBSCAN algorithm that only checks if the candidate points are outliers
     *
     * @param eps             distance limit for DBSCAN
     * @param minNeighbors    minimum neighbors to be within @eps distance for a point to be part of a cluster
     * @param candidatePoints points that will be evaluated if they are cluster members or outliers
     * @param refPoints       reference points to use when evaluating the candidate points
     * @return outliers points- points that did not belong to any cluster of the ref points
     */
    def DBSCAN(eps: Double, minNeighbors: Int)(candidatePoints: Seq[Array[Double]],
      refPoints: Seq[Seq[Array[Double]]]): Seq[Array[Double]] = {

      VedaDbscanLib.returnOutliers(candidatePoints.asJava, refPoints.map(_.asJava).asJava, eps, minNeighbors).asScala
    }

    /**
     * THIS IS ONLY A POC!!
     * Clustering function should be used as a parameter to the density clustering detection model.
     * Applies a clust4j HDBSCAN algorithm:
     * https://github.com/tgsmith61591/clust4j/blob/master/src/main/java/com/clust4j/algo/HDBSCAN.java
     * Notes:
     * 1. no optimizations has been done on the algorithm
     * 2. all the points (including ref points) are being classified
     * 3. due to lib implementation only an integer amount of points is possible
     *
     * @param minCorePoints               minimal num of points in each point core distance (HDBSCAN term)
     * @param minClusterSizeForExtraction cluster extraction parameter (HDBSCAN term)
     * @param candidatePoints             points that will be evaluated if they are cluster members or outliers
     * @param refPoints                   reference points to use when evaluating the candidate points
     * @return outliers points- points that did not belong to any cluster of all the points
     */
    def HDBSCAN(minCorePoints: Int, minClusterSizeForExtraction: Int)(candidatePoints: Seq[Array[Double]],
      refPoints: Seq[Seq[Array[Double]]]): Seq[Array[Double]] = {

      Clust4jHDBSCAN.returnOutliers(candidatePoints.asJava, refPoints.map(_.asJava).asJava,
        minCorePoints, minClusterSizeForExtraction).asScala
    }

    def DBSCANFunction(): Function2[Double, Int,
      Function2[Seq[Array[Double]], Seq[Seq[Array[Double]]], Seq[Array[Double]]]] =
      DBSCAN

    def DBSCANFunction(eps: Double, minNeighbors: Int): Function2[Seq[Array[Double]],
      Seq[Seq[Array[Double]]], Seq[Array[Double]]] =
      DBSCAN(eps, minNeighbors)

    def HDBSCANFunction(): Function2[Int, Int,
      Function2[Seq[Array[Double]], Seq[Seq[Array[Double]]], Seq[Array[Double]]]] =
      HDBSCAN

    def HDBSCANFunction(minCorePoints: Int, minNeighbors: Int): Function2[Seq[Array[Double]],
      Seq[Seq[Array[Double]]], Seq[Array[Double]]] =
      HDBSCAN(minCorePoints, minNeighbors)
  }
}
