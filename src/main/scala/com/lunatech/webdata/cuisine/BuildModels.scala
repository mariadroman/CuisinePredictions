package com.lunatech.webdata.cuisine

import com.lunatech.webdata.cuisine.mllib.trainers._
import com.lunatech.webdata.cuisine.mllib.{MulticlassMetrix, FlowData, Model}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Build the model for the given Trainers and save the models
  */
object BuildModels extends SparkRunner {

  def main(args: Array[String]) = {

    val defConf = new SparkConf(true)
    val conf = defConf.setAppName("CuisineRecipesImportData").
      setMaster(defConf.get("spark.master",  "local[*]"))

    implicit val sc = new SparkContext(conf)
    implicit val configuration = Configuration(args)

    run

  }

  def run(implicit sc: SparkContext, configuration: Configuration) = {

    import DaoUtils._

    // Load the flow data
    val flowData = FlowData.load(configuration.dataPath)

    def train[T](trainer: Trainer[T]): (Model[T], MulticlassMetrix) =
      trainer.trainEvaluate(flowData)

    val trainers = List(LogisticRegressionTrainer(), NaiveBayesTrainer()
      , DecisionTreeTrainer(), RandomForestTrainer()
    )

    val trainingResults = trainers.map(train(_))

    // Train the models and save the models for later use
    trainingResults.map { case (model, metrics) =>
      saveModel(model)
    }

    // Print the models evaluation (GitHub friendly)
    trainingResults.foreach { case (model, metrics) =>
      println(s"\n### ${model.name} model evaluation")
      printEvaluationMetrix(metrics)
      saveMetrix(model, metrics)
    }

  }

}
