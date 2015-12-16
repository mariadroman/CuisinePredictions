package com.lunatech.webdata.cuisine

import org.apache.spark.{SparkContext, SparkConf}

/**
 *
 */
object MainMlLib {

  def main(args: Array[String]) = {

    val defConf = new SparkConf(true)
    val conf = defConf.setAppName("CuisineRecipesImportData").
      setMaster(defConf.get("spark.master",  "local[*]"))

    implicit val sc = new SparkContext(conf)
    implicit val configuration = Configuration(args)

    run
  }

  def run(implicit sc: SparkContext, configuration: Configuration) = {

    val runners = List(ImportData, BuildModels, BuildPredictions, ExportToCSV)

    runners.foreach(_.run)
  }

}
