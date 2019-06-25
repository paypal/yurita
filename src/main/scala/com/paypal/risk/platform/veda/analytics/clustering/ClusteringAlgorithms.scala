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
package com.paypal.risk.platform.veda.analytics.clustering

import com.paypal.risk.platform.veda.analytics.anomaly.clustering.{Clust4jDBSCAN, Clust4jHDBSCAN, Clustering}

import scala.collection.JavaConverters._

object ClusteringAlgorithms {
  /**
   * THIS IS ONLY A POC!!
   * Clustering function should be used as a parameter to the density clustering detection model.
   * Applies clust4j DBSCAN algorithm
   * Notes:
   * 1. no optimizations has been done on the algorithm
   * 2. all the points (including ref points) are being classified
   * 3. due to lib implementation only an integer amount of points is possible
   *
   * @param eps                distance limit for DBSCAN
   * @param minNeighbors       minimum neighbors to be within @eps distance for a point to be part of a cluster
   * @param latestWindowPoints points in the most recent time window
   * @param refPoints          reference points to use when evaluating the clustering
   * @return full clustering of all input points
   */
  def DBSCAN(eps: Double, minNeighbors: Int)(latestWindowPoints: Seq[Array[Double]],
    refPoints: Seq[Seq[Array[Double]]]): Clustering = {

    Clust4jDBSCAN.cluster(latestWindowPoints.asJava, refPoints.map(_.asJava).asJava, eps, minNeighbors)
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
   * @param latestWindowPoints          points in the most recent time window
   * @param refPoints                   reference points to use when evaluating the clustering
   * @return full clustering of all input points
   */
  def HDBSCAN(minCorePoints: Int, minClusterSizeForExtraction: Int)(latestWindowPoints: Seq[Array[Double]],
    refPoints: Seq[Seq[Array[Double]]]): Clustering = {

    Clust4jHDBSCAN.cluster(latestWindowPoints.asJava, refPoints.map(_.asJava).asJava,
      minCorePoints, minClusterSizeForExtraction)
  }

  def DBSCANFunction(eps: Double, minNeighbors: Int):
    Function2[Seq[Array[Double]], Seq[Seq[Array[Double]]], Clustering] =
    DBSCAN(eps, minNeighbors)

  def HDBSCANFunction(minCorePoints: Int, minNeighbors: Int):
    Function2[Seq[Array[Double]], Seq[Seq[Array[Double]]], Clustering] =
    HDBSCAN(minCorePoints, minNeighbors)
}
