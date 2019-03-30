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

// container class for holding a clustering algorithm result
public class Clustering {
  private String algName;
  private int numClusters;
  private int numNoisePoints;
  private int[] labels;

  public Clustering(String algName, int numClusters, int numNoisePoints, int[] labels) {
    this.algName = algName;
    this.numClusters = numClusters;
    this.numNoisePoints = numNoisePoints;
    this.labels = labels;
  }

  public String getAlgName() {
    return algName;
  }

  public int getNumClusters() {
    return numClusters;
  }

  public int getNumNoisePoints() {
    return numNoisePoints;
  }

  public int[] getLabels() {
    return labels;
  }
}
