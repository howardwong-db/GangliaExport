
// scalastyle:off println
package com.spark.sample

import com.databricks.gangliaexport.GangliaExport
import com.databricks.gangliaexport.GangliaClient

import scala.math.random

import org.apache.spark.sql.SparkSession

/** Computes an approximation to pi */
object SparkPi {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      //.master("local[2]")
      .appName("Spark Pi")
      .getOrCreate()
      
    val f = GangliaExport.exportMetrics(spark,"parquet", Map.empty[String,String],"/tmp/gangliaexport", 5)

    val slices = if (args.length > 0) args(0).toInt else 2
    val n = math.min(100000L * slices, Int.MaxValue).toInt // avoid overflow
    val count = spark.sparkContext.parallelize(1 until n, slices).map { i =>
      val x = random * 2 - 1
      val y = random * 2 - 1
      if (x*x + y*y <= 1) 1 else 0
    }.reduce(_ + _)
    println(s"Pi is roughly ${4.0 * count / (n - 1)}")
    
    f.cancel(false)
    //spark.stop()
  }
}
// scalastyle:on println

