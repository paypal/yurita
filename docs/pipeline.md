# Windowing

| type | example | description |
|------|---------|-------------|
|Global Window| Window.global| A single bucket for all events|
|Fixed Windows| Window.fixed(“1 hour”)|Creates windows of fixed duration (no overlap)|
|Sliding Window|Window.sliding(“1 hour”, “10 minutes”)|Creates a sliding window of fixed size, sliding every specified duration (with overlap)|

Fixed windows are the best practice for production since Events will get replicated for each window slide they belong to. Small sliding steps can have a significant performance impact



User can also create their own custom windowing by extending the WindowStrategy trait.

# Aggregation

An aggregation is made of 2 complimentary classes:

Aggregation: the aggregation data structure that will process each window in the pipeline
Aggregation Factory: initializes a new aggregation in each new window that is detected


The design allows to create aggregations in many different patterns (e.g. sharing state between aggregations).
Aggregations contains metadata that contains system and user provided information



Creating custom aggregations by extending this trait:

```
/** factory for creating new data aggrs */
trait DataAggregationFactory extends Serializable {
 
  /** creates a new data aggr when required by window strategy */
  def createNewAggregation(): DataAggregation
}
 
/** modeling of the data inside each window, can contain any custom logic */
trait DataAggregation extends Serializable {
 
  val metadata = new mutable.HashMap[String, String]()
 
  /** adds a new metric observation to the aggr, no order of metrics is guaranteed */
  def update(trackedMetric: TrackedMetric): Unit
 
  /**
   * cleans and compacts unneeded state,
   * can be used after detection or in the future automatically at the background
   */
  def cleanAndCompact(): Unit = {}
}
```

# Window Reference

Many times it is not possible to classify anomalies by absolute definitions therefore intuitively we say an anomaly is "an observation that is significantly different from what we expected".
Since anomalies can have many different patterns we would need a mechanism that can understand the historical behavior of the data taking into account seasonality and trend.

Window Reference allows us to have strategies for selecting historical windows as reference data when we evaluate if a the current window is anomalous.


