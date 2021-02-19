// Databricks notebook source
// MAGIC %md
// MAGIC # GangliaExport
// MAGIC 
// MAGIC https://github.com/howardwong-db/GangliaExport

// COMMAND ----------

import com.databricks.gangliaexport.GangliaExport
//Load the lib into the all purpose cluster
val f = GangliaExport.exportMetrics(spark,"delta", Map.empty[String,String],"/tmp/howard.wong@databricks.com/gangliametricstest", 30)

// COMMAND ----------

//cancel when done
f.cancel(false)

// COMMAND ----------

val metrics = spark.read.format("delta").load("/tmp/howard.wong@databricks.com/gangliametricstest")
metrics.createOrReplaceTempView("metrics")

// COMMAND ----------

// MAGIC %sql
// MAGIC select from_unixtime(reportingTime) as reportingTime, metricName, (100 - metricVal) as cpupercent from metrics where metricName = "cpu_idle" and clusterName = 'exporttest' order by reportingTime

// COMMAND ----------

// MAGIC %sql
// MAGIC select * from metrics where metricName like "cpu%" order by reportingTime

// COMMAND ----------

// DBTITLE 1,CPU Usage
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
// MAGIC       and reportingTime BETWEEN DATE '2020-01-01' AND DATE '2021-01-31'
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

// DBTITLE 1,Memory Usage
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
