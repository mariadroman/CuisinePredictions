package com.lunatech.webdata.cuisine

import org.apache.spark.{SparkContext, SparkConf}

/**
 *
 */
object MainMlLib {

  def main(args: Array[String]) = {

    val conf = new SparkConf().setAppName("CuisineRecipesImportData").
      setMaster("local[*]")

    implicit val sc = new SparkContext(conf)

    val runners = List(ImportData, BuildModels, BuildPredictions)

    runners.foreach(_.run)
  }

}
