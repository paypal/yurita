We extend Spark's DataFrame API by adding the workload to the computation graph.

```
// import the Dataframe api extension
import com.paypal.risk.platform.veda.analytics.Implicits._
 
// add workload to computation
df.detectAnomalies(workload)

```
