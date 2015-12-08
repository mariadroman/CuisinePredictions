package com.lunatech.webdata.cuisine

import com.lunatech.webdata.cuisine.mllib.TrainingDataImporter
import com.lunatech.webdata.cuisine.mllib.transformers.ChiSqSelectorTransformer
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Import training data from files and save the LabeledVectors
  * along with the label and feature mappings.
  */
object ImportData extends SparkRunner {

   def main(args: Array[String]) = {

     val conf = new SparkConf().setAppName("CuisineRecipesImportData").
       setMaster("local[*]")

     implicit val sc = new SparkContext(conf)

     run

   }

  def run(implicit sc: SparkContext) = {

    // Import the data and process it
    val flowData = TrainingDataImporter
      .importFrom(Configuration.inputTrainingData)
    // TODO: Fix the transformation
      .map(ChiSqSelectorTransformer(0.8).transform)

    // Store the flow data for later processing
    flowData.save(Configuration.dataPath)

  }

 }
