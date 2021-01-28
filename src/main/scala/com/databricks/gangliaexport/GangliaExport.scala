package com.databricks.gangliaexport

import scala.math.random

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.DataFrame
import java.util.concurrent._
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._

/**
 * Handler to exports Ganglia metrics
 */
object GangliaExport {
  /**
   * Collects Ganglia metrics and returns a DataFrame
   * @param spark SparkSession object
   * @param gc GangliaClient to connect to
   * @return a DataFrame of the Ganglia metrics
   */
  def collectMetrics(spark: SparkSession, gc: GangliaClient) = {
    //get Databricks cluster mame
    import spark.implicits._

    val dbClusterName = spark.conf.get("spark.databricks.clusterUsageTags.clusterName","local")
    val dbClusterId = spark.conf.get("spark.databricks.clusterUsageTags.clusterId","local")
    val cs = gc.clusterStats(dbClusterName, dbClusterId)  
    val df = cs.metrics.toDF().withColumn("reportingDate", to_date($"reportingTime".cast(TimestampType)))
    df
  }

  /**
   * Exports the Ganglia metrics to any Spark supported datasource periodically.
   * This uses a ThreadPool to run periodically. The writes will be partitioned by reporting date.
   *
   * @param spark SparkSession
   * @param format Spark format to save
   * @param options any options for the format
   * @param path the path to save the metrics
   * @param exportPeriod interval to export in seconds
   * @param endPoint Ganglia web service endpoint
   * @return a ScheduledFuture to track the ScheduledThreadPoolExecutor
   */
  def exportMetrics(spark: SparkSession, format: String, options:Map[String, String], path:String, exportPeriod:Long = 30, endPoint:String = "http://localhost:8652") = {
    val gc = new GangliaClient(endPoint)
    val ex = new ScheduledThreadPoolExecutor(1)
    val task = new Runnable { 
      def run() =  {
        var df = collectMetrics(spark, gc)
        df.write.format(format).partitionBy("reportingDate").options(options).mode("append").save(path)
      }
    }
    val f = ex.scheduleAtFixedRate(task, 1, exportPeriod, TimeUnit.SECONDS)
    f
  }
  
}
// scalastyle:on println