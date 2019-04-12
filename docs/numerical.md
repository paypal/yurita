# Overview

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

