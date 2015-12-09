package com.lunatech.webdata.cuisine.mllib.trainers

import com.lunatech.webdata._
import com.lunatech.webdata.cuisine._
import com.lunatech.webdata.cuisine.mllib.FlowData
import org.apache.spark.mllib.classification.{NaiveBayes, NaiveBayesModel}
import org.apache.spark.{SparkConf, SparkContext}

/**
 *
 * @param modelType acceptable values: "multinomial" or "bernoulli"
 */
class NaiveBayesTrainer(lambda: Double = 1.0, modelType: String = "multinomial") extends Trainer[NaiveBayesModel] {

  def train(flowData: FlowData)(implicit sc: SparkContext) = {

    val trainingData = flowData.data

    NaiveBayes.train(trainingData, lambda, modelType)

  }

}

object NaiveBayesTrainer {

  def apply() = new NaiveBayesTrainer()

  def main(args: Array[String]) = {

    val conf = new SparkConf(true).setAppName(this.getClass.getSimpleName).
      setMaster("local[*]")

    implicit val sc = new SparkContext(conf)
    val configuration = Configuration(args)

    val flowData = FlowData.load(configuration.dataPath)

    val (model, metrics) = NaiveBayesTrainer().trainEvaluate(flowData)

    removeFile(configuration.naiveBayesPath)
    model.save(configuration.naiveBayesPath)

    println(s"### ${model.self.getClass.getSimpleName} model evaluation")

    printEvaluationMetrix(metrics)

  }

}
