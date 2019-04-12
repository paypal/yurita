# Built in Models

Users can choose to plugin custom models or use built-in generic base models. As development progress more and more generic base models will be added.

![builtin](images/modelList.png)

# Categorical Models

Data values are divided to clear categories, check which values in a given column showed abnormal behavior. the model computes an anomaly score and also outputs each value's contribution to that score in order to extract the cause of the anomaly.
Users can provide the statistical function for the model or use built-in functions.

___Built-in statistical functions:___

|function|description|
|--------|-----------|
|Entropy|![sum](images/sumfn.png)|

The model allows also to plug-in a function how to merge the reference windows into a single reference aggregation before comparing it to the current aggregation that we want to evaluate. It allows also to plugin-in a function that will decide if the statistical result is an anomaly.

![fnflow](images/fnflow.png)

___Built-in reference aggregation functions:___

|function|description|
|--------|-----------|
|avgRef|creates a reference aggregation that is the average for each value in the historical references|

___Built-in threshold functions:___

|function|description|
|--------|-----------|
|statResultThreshold|decides if the statistical function result crosses a threshold then classify it as an anomaly|

Creating a model with a built-in function:

```
val categoricalPipes = PipelineBuilder()
  .onColumns(…)
  .buildCategoricalModel(
    Functions.Categorical.avgRef,
    Functions.Categorical.entropy,
    Functions.statResultThreshold(3.0))
```

creating a model with a custom function:

```
val categoricalPipes = PipelineBuilder()
  .onColumns(…)
  .buildCategoricalModel(
    Functions.Categorical.avgRef,
    myCustomFunction,
    Functions.statResultThreshold(3.0))
```
# Numerical Models

Track continuous numerical data points and detect points that are statistically significant.
Users can provide the statistical function for the model or use built-in functions.

___Built-in statistical functions:___

|function|description|
|--------|-----------|
|Z-score|![zscore](images/zfn.png)|

The model allow also to specify how to merge the reference windows into a reference statistical value before comparing it to the current aggregation that we want to evaluate. It allows also to plugin-in a function that will decide if the statistical result is an anomaly.

![fnflow](images/fnflow.png)

___Built-in reference values functions:___

|function|description|
|--------|-----------|
|avgMeanAbdStdRef|creates reference values: the "average of averages" and average stdev of the historical windows|

___Built-in threshold functions:___

|function|description|
|--------|-----------|
|simpleThreshold|decides if the statistical function result crosses a threshold then classify it as an anomaly|

Creating a model with a built-in function:

```
val numericalPipes = PipelineBuilder()
  .onColumns(…)
  .buildNumericalModel(
    Functions.Numerical.avgMeanAndStdevRef[Double],
    Functions.Numerical.zScore[Double],
    Functions.simpleThreshold(3.0))
```
	
Creating a model with a custom function:

```
val numericalPipes = PipelineBuilder()
  .onColumns(…)
  .buildNumericalModel(
    Functions.Numerical.avgMeanAndStdevRef[Double],
    myCustomFunction,
    Functions.simpleThreshold(3.0)
```

# Density Based Cluster Models

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

