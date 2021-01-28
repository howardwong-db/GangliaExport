name := "GangliaExport"
version := "1.0"
scalaVersion := "2.11.12"
val sparkVersion = "2.4.5"
// Note the dependencies are provided
libraryDependencies += "org.apache.spark" %% "spark-core" % sparkVersion % "provided"
libraryDependencies += "org.apache.spark" %% "spark-sql" % sparkVersion % "provided"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.0" % "test"
libraryDependencies += "com.github.tomakehurst" % "wiremock-standalone" % "2.23.2" % "test"

//unmanagedBase := new java.io.File("/usr/local/anaconda3/envs/dbconnect/lib/python3.7/site-packages/pyspark/jars")
//for testing
mainClass := Some("com.spark.sample.SparkPi")

// Do not include Scala in the assembled JAR
assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)

// META-INF discarding for the FAT JAR
assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}

