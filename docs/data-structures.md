To allow model execution to be scalable and robust, the framework internally uses streaming oriented data structures

# Data containers
Data structures for holding historical windows data

Containers API expose efficient statistics (e.g. average, stdev) 

Containers internally cache calculations to avoid re-computation and boost performance

# Container types

Uniform Sampling (default) – stores only a uniform sample of the data points in a window, uses Reservoir Sampling algorithm



Sorted Uniform Sampling – same as Uniform Sampling but points are also ordered, supports more advanced statistics like median and percentiles approximation
*currently supports only  long values

Raw – stores all data points, memory intensive and not recommended for production

**Models that use data containers have additional parameters:**

|param|default|description|
|-----|-------|-----------|
|containerType|Uniform Sampling| the type of container to use|
|maxWindowCapacity|10,000| the maximal raw events to keep in each window container
|

# Data Frame API

We extend Spark's DataFrame API by adding the workload to the computation graph.

```
// import the Dataframe api extension
import com.paypal.risk.platform.veda.analytics.Implicits._
 
// add workload to computation
df.detectAnomalies(workload)

```
