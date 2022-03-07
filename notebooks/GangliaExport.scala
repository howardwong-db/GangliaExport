// Databricks notebook source
// MAGIC %md
// MAGIC # GangliaExport
// MAGIC 
// MAGIC https://github.com/howardwong-db/GangliaExport
// MAGIC 
// MAGIC Make sure that the GangliaExport lib is added to the attached cluster

// COMMAND ----------

// DBTITLE 1,Collect Metrics
import com.databricks.gangliaexport.GangliaExport
//store in Delta format at 30 sec interval
val f = GangliaExport.exportMetrics(spark,"delta", Map.empty[String,String],"/tmp/howard.wong@databricks.com/gangliametricstest", 30)

// COMMAND ----------

import org.apache.spark.sql.functions._
//do something
val df = spark.range(0,100000).withColumn("partBy", col("id") % 10)
df.createOrReplaceTempView("tmptable")
spark.sql("select sum(id) from tmptable")

// COMMAND ----------

//cancel when done
f.cancel(false)

// COMMAND ----------

val metrics = spark.read.format("delta").load("/tmp/howard.wong@databricks.com/gangliametricstest")
metrics.createOrReplaceTempView("metrics")

// COMMAND ----------

// DBTITLE 1,CPU Load
// MAGIC %sql
// MAGIC select
// MAGIC   from_unixtime(reportingTime) as reportingTime,
// MAGIC   metricName,
// MAGIC   (100 - metricVal) as cpupercent
// MAGIC from
// MAGIC   metrics
// MAGIC where
// MAGIC   metricName = "cpu_idle"
// MAGIC   and clusterName = 'hwong10_2demo'
// MAGIC order by
// MAGIC   reportingTime

// COMMAND ----------

// DBTITLE 1,CPU usage - Chart using series grouping by metricName
// MAGIC %sql
// MAGIC select
// MAGIC   from_unixtime(reportingTime) as reportingTime,
// MAGIC   clusterName,
// MAGIC   metricName,
// MAGIC   metricVal
// MAGIC from
// MAGIC   metrics
// MAGIC where
// MAGIC   metricName like 'cpu%'
// MAGIC   and metricUnits = '%'
// MAGIC   and clusterName = 'exporttest'
// MAGIC   and reportingDate BETWEEN DATE '2020-01-01'
// MAGIC   AND DATE '2021-01-31'
// MAGIC order by
// MAGIC   reportingTime,
// MAGIC   clusterName,
// MAGIC   metricName

// COMMAND ----------

// DBTITLE 1,CPU Usage - Chart using pivot (metricVal from rows to columns)
// MAGIC %sql
// MAGIC select
// MAGIC   *
// MAGIC from
// MAGIC   (
// MAGIC     select
// MAGIC       from_unixtime(reportingTime) as reportingTime,
// MAGIC       clusterName,
// MAGIC       metricName,
// MAGIC       metricVal
// MAGIC     from
// MAGIC       metrics
// MAGIC     where
// MAGIC       metricName like 'cpu%'
// MAGIC       and metricUnits = '%'
// MAGIC       and clusterName = 'exporttest'
// MAGIC       and reportingDate BETWEEN DATE '2020-01-01'
// MAGIC       AND DATE '2021-01-31'
// MAGIC   ) pivot (
// MAGIC     avg(metricVal) for metricName in (
// MAGIC       'cpu_idle' cpu_idle,
// MAGIC       'cpu_system' cpu_system,
// MAGIC       'cpu_user' cpu_user,
// MAGIC       'cpu_nice' cpu_nice
// MAGIC     )
// MAGIC   )
// MAGIC order by
// MAGIC   reportingTime

// COMMAND ----------

// DBTITLE 1,Memory Usage - Chart using Pivot
// MAGIC %sql
// MAGIC select
// MAGIC   *
// MAGIC from
// MAGIC   (
// MAGIC     select
// MAGIC       from_unixtime(reportingTime) as reportingTime,
// MAGIC       clusterName,
// MAGIC       metricName,
// MAGIC       metricVal
// MAGIC     from
// MAGIC       metrics
// MAGIC     where
// MAGIC       metricName like 'mem%'
// MAGIC       and clusterName = 'exporttest'
// MAGIC   ) pivot (
// MAGIC     sum(metricVal) for metricName in (
// MAGIC       'mem_total' mem_total,
// MAGIC       'mem_free' mem_free,
// MAGIC       'mem_cached' mem_cached,
// MAGIC       'mem_shared' mem_shared,
// MAGIC       'mem_buffers' mem_buffers
// MAGIC     )
// MAGIC   )
// MAGIC order by
// MAGIC   reportingTime
