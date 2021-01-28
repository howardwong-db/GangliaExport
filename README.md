# GangliaExport

Ganglia is installed on the head node of each cluster.
This notebook shows how we can export Ganglia metrics to cloud storage in any Spark format which can be used to analyze cluster utilization.

## Usage
This code has to be embedded in the driver of the Spark job.
Load the ganglia eport lib into the all purpose cluster or add as a dependency lib in your job
```scala
import com.databricks.gangliaexport.GangliaExport
val f = GangliaExport.exportMetrics(spark,"parquet", Map.empty[String,String],"/tmp/howard.wong@databricks.com/gangliametricstest", 30)
```

Collect as DataFrame
```scala
val gc = new GangliaClient(gUrl)
var df = GangliaExport.collectMetrics(spark, gc)
df.write.format("csv").partitionBy("reportingDate").mode("append").save("/tmp/testexport")
```
## Benefits
* Metrics data are stored in low cost cloud storage in any Spark format. 
* No other monitoring dependencies which reduces complication and cost.
* Data is persisted even after cluster is terminated.
