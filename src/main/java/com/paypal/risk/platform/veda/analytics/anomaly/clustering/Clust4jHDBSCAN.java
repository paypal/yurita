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

package com.paypal.risk.platform.veda.analytics.anomaly.clustering;

import com.clust4j.algo.HDBSCAN;
import com.clust4j.algo.HDBSCANParameters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;



/**
 * POC of running HDBSCAN using the clustj lib.
 */
public class Clust4jHDBSCAN {

  public static void main(String[] args) {
    run();
  }

  private static double[] point(double n) {
    return new double[] {n, n, n};
  }

  private static List<double[]> clusterGen(int size) {
    Random r = new Random();
    List<double[]> res = new ArrayList<>();
    int buff = r.nextInt(10000);
    for (int i = 0; i < size; i++) {
      double n = r.nextInt(10);
      res.add(point(buff + n));
    }
    return res;
  }

  private static double[][] listToArr(List<double[]> points) {

    double[][] data = new double[points.size()][points.get(0).length];
    for (int i = 0; i < points.size(); i++) {
      data[i] = points.get(i);
    }
    return data;
  }

  private static double[][] getExampleData() {
    List<double[]> c1 = clusterGen(10);
    List<double[]> c2 = clusterGen(10);
    c1.addAll(c2);
    c1.add(point(100000));
    c1.add(point(200000));
    c1.add(point(0));
    return listToArr(c1);
  }

  private static void run() {
    final Array2DRowRealMatrix mat = new Array2DRowRealMatrix(getExampleData());

    HDBSCAN hdb = new HDBSCANParameters(5)
        .setMinClustSize(5)
        .fitNewModel(mat);

    StringBuilder sb = new StringBuilder("\n");
    for (int i = 0; i < hdb.getLabels().length; i++) {
      sb.append("#" + i + "(" + mat.getRow(i)[0] + ")" + ": " + hdb.getLabels()[i] + "\n");
    }

    String s = new StringBuilder()
        .append("name: " + hdb.getName() + "\n")
        .append("clusters: " + hdb.getNumberOfIdentifiedClusters() + "\n")
        .append("noise points: " + hdb.getNumberOfNoisePoints() + "\n")
        .append("labels: " + sb + "\n")
        .toString();

    System.out.println(s);
  }

  private static double[][] mergeInputToArr(List<double[]> points,
                                            List<List<double[]>> refPoints) {
    // merge all points to a single matrix
    // the int here is too small for production but library requires a double[][] which
    // support only int dimensions
    int pointsCount = points.size();
    for (List<double[]> reflist : refPoints) {
      pointsCount += reflist.size();
    }
    double[][] matrix = new double[pointsCount][points.get(0).length];
    int i = 0;
    for (double[] p : points) {
      matrix[i++] = p;
    }
    for (Iterable<double[]> reflist : refPoints) {
      for (double[] p : reflist) {
        matrix[i++] = p;
      }
    }
    return matrix;
  }

  private static List<double[]> extractNoisePoints(List<double[]> candidatePoints,
                                                   Array2DRowRealMatrix pointsMatrix,
                                                   int[] clusterLabels) {
    int noiselabel = -1;
    List<double[]> noisePoints = new ArrayList<>();
    for (int i = 0; i < candidatePoints.size(); i++) {
      if (clusterLabels[i] == noiselabel) {
        noisePoints.add(pointsMatrix.getRow(i));
      }
      if (!Arrays.equals(candidatePoints.get(i), pointsMatrix.getRow(i))) {
        System.out.println("Warning points to not match!");
      }
    }
    return noisePoints;
  }

  /**
   * Returns a list of outliers from clustering.
   */
  public static List<double[]> returnOutliers(List<double[]> candidatePoints,
                                              List<List<double[]>> refPoints,
                                              int minCorePoints,
                                              int minClusterSizeForExtraction) {

    Array2DRowRealMatrix pointsMatrix = new Array2DRowRealMatrix(mergeInputToArr(candidatePoints,
        refPoints));
    HDBSCAN hdb = new HDBSCANParameters(minCorePoints)
        .setMinClustSize(minClusterSizeForExtraction)
        .fitNewModel(pointsMatrix);

    return extractNoisePoints(candidatePoints, pointsMatrix, hdb.getLabels());
  }

  /**
   * Creates a clustering container class that defines the clustering algorithm specifications.
   */
  public static Clustering cluster(List<double[]> candidatePoints,
                                   List<List<double[]>> refPoints,
                                   int minCorePoints,
                                   int minClusterSizeForExtraction) {

    Array2DRowRealMatrix pointsMatrix = new Array2DRowRealMatrix(mergeInputToArr(candidatePoints,
        refPoints));
    HDBSCAN hdb = new HDBSCANParameters(minCorePoints)
        .setMinClustSize(minClusterSizeForExtraction)
        .fitNewModel(pointsMatrix);

    return new Clustering(hdb.getName(),
        hdb.getNumberOfIdentifiedClusters(),
        hdb.getNumberOfNoisePoints(),
        hdb.getLabels());
  }
}
