package com.databricks.gangliaexport

import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Suite}
import org.apache.spark.sql.SparkSession

trait SharedSparkSession extends BeforeAndAfterAll with BeforeAndAfterEach {
  self: Suite =>
    
  @transient var spark : SparkSession = _
  
  def appID: String = (this.getClass.getName 
      + math.floor(math.random * 10E4).toLong.toString)

  override def beforeAll() {
    super.beforeAll()
    spark = SparkSession.builder().appName("SparkUnitTesting")
      .master("local[*]")
      .config("spark.ui.enabled", "false")
      .config("spark.app.id", appID)
      .getOrCreate()    
  }

  override def afterAll() {
    super.afterAll()
    spark.stop()    
  }
}


