<!--
 Copyright 2019 PayPal Inc

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->

Detect outlier points (points that don’t belong to any cluster) using density based clustering algorithms on multi-dimensional data points. These methods allow us to take advantage of hidden correlation between between features.
Users can provide the clustering functions for the model or use built-in functions.

Built-in statistical functions:

|function|description|
|--------|-----------|
|DBSCAN|applies an optimized version of the algorithm that only detect outliers|
|HDBSCAN|applies full clustering and including dynamic optimizations like kd-trees, ball-trees, prim's MST, boruvka MST|

Creating a model with a built-in function:

```
val clusteringPipes = PipelineBuilder()
  .onColumns(…)
  .buildDensityClusteringModel(Functions.Clustering.DBSCAN(2, 2))
```

```
val clusteringPipes = PipelineBuilder()
  .onColumns(…)
  .buildDensityClusteringModel(myCustomFunction)
```
