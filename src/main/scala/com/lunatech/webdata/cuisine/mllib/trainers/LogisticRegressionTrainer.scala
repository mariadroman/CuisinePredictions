package com.lunatech.webdata.cuisine.mllib.trainers

import com.lunatech.webdata._
import com.lunatech.webdata.cuisine.Configuration
import com.lunatech.webdata.cuisine.mllib.{FlowData, Trainer}
import org.apache.spark.mllib.classification.{LogisticRegressionModel, LogisticRegressionWithLBFGS}
import org.apache.spark.{SparkConf, SparkContext}

/**
  *
  */
class LogisticRegressionTrainer() extends Trainer[LogisticRegressionModel] {

  def train(flowData: FlowData)(implicit sc: SparkContext) = {

    val numClasses = flowData.labelToIndex.size + 1

    val trainingData = flowData.data

    new LogisticRegressionWithLBFGS()
      .setNumClasses(numClasses)
      .run(trainingData)
  }

}
object LogisticRegressionTrainer {

  def apply() = new LogisticRegressionTrainer

  def main(args: Array[String]) = {

    val conf = new SparkConf(true).setAppName(this.getClass.getSimpleName).
      setMaster("local[*]")

    implicit val sc = new SparkContext(conf)

    val flowData = FlowData.load(Configuration.dataPath)

    val (model, metrics, runtime) = LogisticRegressionTrainer().trainEvaluate(flowData)

    removeDir(Configuration.logisticRegPath)
    model.save(Configuration.logisticRegPath)

    printEvaluationMetrics(model, metrics)
    println(s"Training for ${model.self.getClass.getSimpleName} was completed in ${runtime/1000} seconds.")
  }


}
