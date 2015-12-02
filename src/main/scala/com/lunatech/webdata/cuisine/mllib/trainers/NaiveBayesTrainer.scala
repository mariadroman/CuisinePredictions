package com.lunatech.webdata.cuisine.mllib.trainers

import com.lunatech.webdata._
import com.lunatech.webdata.cuisine.Configuration
import com.lunatech.webdata.cuisine.mllib.{FlowData, Trainer}
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

    val flowData = FlowData.load(Configuration.dataPath)

    val (model, metrics, runtime) = NaiveBayesTrainer().trainEvaluate(flowData)

    removeDir(Configuration.naiveBayesPath)
    model.save(Configuration.naiveBayesPath)

    printEvaluationMetrics(model, metrics)
    println(s"Training for ${model.self.getClass.getSimpleName} was completed in ${runtime/1000} seconds.")
  }

}
