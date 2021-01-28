package com.databricks.gangliaexport


import org.scalatest.funsuite.AnyFunSuite
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import org.apache.commons.io.FileUtils
import java.io.File
import org.scalatest.Ignore


class GangliaExportTest extends AnyFunSuite with SharedSparkSession with WireMockSupport {  
  
  test("test Ganglia export") {
    
    val gUrl = "http://localhost:%d".format(wireMockServer.port())

    //trick the blob storage asset to use a local path for testing
    //by setting the url to a local path
    val fileLocation = getClass.getResource("/samplegangliaresp.xml").getPath
    val source = scala.io.Source.fromFile(fileLocation)
    val gangliaxml = try source.mkString finally source.close()

    val outputPath = "/tmp/testexport"

    //return blob asset by uuid        
    wireMockServer.stubFor(
      get(urlPathMatching("/cluster"))
      .willReturn(aResponse()
      //.withHeader("Content-Type", "application/json")
      .withBody(gangliaxml)
      .withStatus(200)))
    
    val f  = GangliaExport.exportMetrics(spark, "parquet", Map.empty[String,String], outputPath, 5, gUrl)
    //val gc = new GangliaClient(gUrl)
    //var df = GangliaExport.collectMetrics(spark, gc)
    //df.write.format("csv").partitionBy("reportingDate").mode("append").save("/tmp/testexport")
    //cleanup the writeLoc file
    Thread.sleep(10000)
    f.cancel(false)
    spark.read.parquet(outputPath).show(5)
    val successFile = FileUtils.getFile(outputPath)
    assert(successFile.exists())
    FileUtils.deleteQuietly(new File(outputPath))
  }
}