package com.databricks.gangliaexport
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Suite}



trait WireMockSupport extends BeforeAndAfterAll with BeforeAndAfterEach{
  me: Suite =>

  val wireMockServer = new WireMockServer(wireMockConfig().dynamicPort())
  
  override def beforeAll(): Unit = {
    super.beforeAll()
    wireMockServer.start()
  }

  override def afterAll(): Unit = {
    wireMockServer.stop()
    super.afterAll()
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    wireMockServer.resetAll()
  }
} 