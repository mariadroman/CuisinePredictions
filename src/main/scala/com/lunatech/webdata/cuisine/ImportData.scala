package com.lunatech.webdata.cuisine

import com.lunatech.webdata.cuisine.mllib.TrainingDataImporter
import org.apache.spark.{SparkConf, SparkContext}

/**
 * Import training data from files and save the LabeledVectors
 * along with the label and feature mappings.
 */
object ImportData extends SparkRunner {

  def main(args: Array[String]) = {

    val defConf = new SparkConf(true)
    val conf = defConf.setAppName("CuisineRecipesImportData").
      setMaster(defConf.get("spark.master",  "local[*]"))

    implicit val sc = new SparkContext(conf)
    implicit val configuration = Configuration(args)

    run

  }

  def run(implicit sc: SparkContext, configuration: Configuration) = {

    // Import the data and process it
    val flowData = TrainingDataImporter
      .importFrom(configuration.inputTrainingData)
    // TODO: Fix the transformation
    //      .map(ChiSqSelectorTransformer(0.8).transform)

    // Store the flow data for later processing
    flowData.save(configuration.dataPath)

  }

}
