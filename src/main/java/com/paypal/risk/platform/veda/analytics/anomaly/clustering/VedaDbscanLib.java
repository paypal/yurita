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

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.ml.distance.EuclideanDistance;



// internal implementation of a customized DBSCAN application
public class VedaDbscanLib {

  /**
   * Will return points that did not belong to any cluster.
   */
  public static List<double[]> returnOutliers(Iterable<double[]> points,
                                              List<List<double[]>> refPoints,
                                              Double eps, int minNeighbors) {
    List<double[]> anomalies = new ArrayList<>();
    for (double[] p : points) {
      int neighbors = 0;
      for (Iterable<double[]> ref : refPoints) {
        for (double[] refP : ref) {
          double distance = new EuclideanDistance().compute(p, refP);
          if (distance <= eps) {
            neighbors += 1;
            if (neighbors == minNeighbors) {
              break;
            }
          }
        }
        if (neighbors == minNeighbors) {
          break;
        }
      }
      if (neighbors < minNeighbors) {
        anomalies.add(p);
      }
    }
    return anomalies;
  }
}
