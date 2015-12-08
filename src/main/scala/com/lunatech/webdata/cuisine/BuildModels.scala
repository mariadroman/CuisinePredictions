package com.lunatech.webdata.cuisine

import com.lunatech.webdata.cuisine.mllib.trainers.{RandomForestTrainer, DecisionTreeTrainer, NaiveBayesTrainer, LogisticRegressionTrainer}
import com.lunatech.webdata.cuisine.mllib.{MulticlassMetrix, FlowData, Model, Trainer}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Build the model for the given Trainers and save the models
  */
object BuildModels extends SparkRunner {

  def main(args: Array[String]) = {

    val conf = new SparkConf().setAppName("CuisineRecipesBuildModels").
      setMaster("local[*]")

    implicit val sc = new SparkContext(conf)

    run

  }

  def run(implicit sc: SparkContext) = {

    import DaoUtils._

    // Load the flow data
    val flowData = FlowData.load(Configuration.dataPath)

    def train[T](trainer: Trainer[T]): (Model[T], MulticlassMetrix) =
      trainer.trainEvaluate(flowData)

    val trainers = List(LogisticRegressionTrainer()
      , NaiveBayesTrainer()
      , DecisionTreeTrainer(), RandomForestTrainer()
    )

    val trainingResults = trainers.map(train(_))

    // Train the models and save the models for later use
    trainingResults.map { case (model, metrics) =>
      saveModel(model)
    }

    // Print the models evaluation (GitHub friendly)
    trainingResults.foreach { case (model, metrics) =>
      println(s"### ${model.name} model evaluation")
      printEvaluationMetrix(metrics)
      saveMetrix(model, metrics)
    }

  }

}
