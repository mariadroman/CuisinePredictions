name := "CuisinePredictionConsumer"

version := "0.1.0"

scalaVersion := "2.10.4"

libraryDependencies += "org.json4s" %% "json4s-jackson" % "3.2.9"

//libraryDependencies += "org.json4s" %% "json4s-native" % "3.2.9"

libraryDependencies += "org.apache.spark" %% "spark-core" % "1.5.2"

libraryDependencies += "org.apache.spark" %% "spark-sql" % "1.5.2"

libraryDependencies += "org.apache.spark" %% "spark-mllib" % "1.5.2"
