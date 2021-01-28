package com.databricks.gangliaexport
import scala.xml.Node
import scala.util.control.Exception.allCatch



trait BaseGangliaMetric {
  def hostName: String
  def hostIp: String
  def reportingTime: Long
  def metricName: String
  def metricType: String
  def metricUnits: String
}

//Use Double for all numeric values to simplify schema
case class GangliaMetric(clusterName: String,
                         clusterId: String,
                         hostName: String,
                         hostIp: String,
                         reportingTime: Long,
                         metricName: String,
                         metricType: String,
                         metricUnits: String,
                         metricVal: Double) extends BaseGangliaMetric

case class ClusterStats(metrics: Seq[GangliaMetric])
/**
 * Connects to Ganglia Web Service endpoint to collect stats
 */
class GangliaClient(endpoint: String = "http://localhost:8652") {
  //Databricks uses "cluster" as the clusterName
  /**
   * Collect stats from Ganglia installed on Databricks Runtime
   * @param dbClusterName The Databricks cluster name
   * @param dbClusterId The Databricks cluster id
   * @param clusterName Ganglia cluster name. Databricks uses "cluster"
   * @return ClusterStats
   */
  def clusterStats(dbClusterName:String, dbClusterId: String, clusterName: String = "cluster"): ClusterStats = {
    // see https://stackoverflow.com/questions/14557546/is-there-a-api-for-ganglia
    // for info on the API and XML schema
    val doc = scala.xml.XML.load(new java.net.URL(s"${endpoint}/$clusterName"))

    
    //debug:
    //val p = new scala.xml.PrettyPrinter(80, 4)
    //println (p.format(doc))
    
    val hostsStats = (doc \\ "HOST").flatMap { h =>
      val hostName = h.attribute("NAME").get.text
      val hostIp = h.attribute("IP").get.text
      val reportingTime = h.attribute("REPORTED").get.text.toLong
      (h \ "METRIC").collect {
        case m: Node if m.attribute("TYPE").exists(t => isDouble(t.text) || isLong(t.text)) =>
          val metricType = m.attribute("TYPE").map(_.text).getOrElse("?")
          val metricName = m.attribute("NAME").map(_.text).getOrElse("?")
          val metricUnits = m.attribute("UNITS").map(_.text).getOrElse("?")
          val metricVal = m.attribute("VAL").map(_.text.toDouble).getOrElse(0.0)
          GangliaMetric(dbClusterName, dbClusterId, hostName, hostIp, reportingTime, metricName, metricType, metricUnits, metricVal)
      }
    }

    ClusterStats(hostsStats)
  }
  //Ganglia METRIC TYPE (string | int8 | uint8 | int16 | uint16 | int32 | uint32 | float | double | timestamp)
  private def isLongNumber(s: String): Boolean = (allCatch opt s.toLong).isDefined
  private def isDoubleNumber(s: String): Boolean = (allCatch opt s.toDouble).isDefined
  private def isMetricNumeric(t: String) = (t == "uint16" || t == "uint32" || t == "int32" || t == "int16" || t == "double" || t == "float" )
  private def isDouble(t: String) = (t == "double" || t == "float")
  private def isLong(t: String) = (t == "uint16" || t == "uint32" || t == "int32" || t == "int16")
  private def isNumeric(input: String): Boolean = input.matches("[+-]?\\d+.?\\d+")
}

