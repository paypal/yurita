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
