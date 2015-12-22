package com.lunatic.mlx.kddcup99

import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}
import org.apache.spark.sql.{Row, SQLContext}
import org.apache.spark.{SparkConf, SparkContext}
import org.elasticsearch.spark.sql._

/**
 * Save the Raw data to ES for analysis
 */
object ExportToESRaw {

  def main(args: Array[String]) = {

    implicit val appConf = Configuration(args)

    val defConf = new SparkConf(true)
    val conf = defConf.
      setAppName(appConf.appPrefix + this.getClass.getSimpleName).
      setMaster(defConf.get("spark.master", "local[*]")).
      set("es.index.auto.create", appConf.es_index_auto_create).
      set("es.nodes", appConf.es_nodes).
      set("es.port", appConf.es_port)

    implicit val sc = new SparkContext(conf)

    val sqc = new SQLContext(sc)

    val lines = sc.textFile(appConf.inputTrainingData).map(_.split(","))

    val colnames = sc.textFile("/tmp/KDD-Cup-1999/kddcup.colnames.txt")
      .map(_.split(":")).map(arr => (arr(0).trim, arr(1).trim == "continuous.")).collect

    colnames.foreach(x => println(s"${x._1}  ${x._2}"))
    println(colnames.size)

    val schema = StructType {
      colnames.map {
        _ match {
          case (cn, true) => StructField(cn, DoubleType, true)
          case (sn, false) => StructField(sn, StringType, true)
        }
      }
    }
    val typedData = lines.map { arr =>
      val values = (0 until arr.size).map { i =>
        val currentValue = arr(i)
        if (colnames(i)._2)
          currentValue.toDouble
        else
          currentValue
      }.toArray
      values
    }

    schema.printTreeString()

    val dataDF = sqc.createDataFrame(typedData.map(a => Row.fromSeq(a)), schema)

    dataDF.saveToEs(appConf.es_index)
  }

}
