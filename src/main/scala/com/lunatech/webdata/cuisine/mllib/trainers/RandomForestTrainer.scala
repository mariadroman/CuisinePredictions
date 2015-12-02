package com.lunatech.webdata.cuisine.mllib.trainers

import com.lunatech.webdata._
import com.lunatech.webdata.cuisine.Configuration
import com.lunatech.webdata.cuisine.mllib.{FlowData, Trainer}
import org.apache.spark.mllib.tree.RandomForest
import org.apache.spark.mllib.tree.model.RandomForestModel
import org.apache.spark.{SparkConf, SparkContext}

/**
 *
 * @param maxDepth
 * @param maxBins
 * @param numTrees
 * @param impurity  acceptable values: "gini" or "entropy"
 * @param featureSubsetStrategy
 */
class RandomForestTrainer(maxDepth: Int = 15,
                          maxBins: Int = 1000,
                          numTrees: Int = 12,
                          impurity: String = "gini",
                          featureSubsetStrategy: String = "auto" )
  extends Trainer[RandomForestModel] {

  def train(flowData: FlowData)(implicit sc: SparkContext) = {

    val numClasses = flowData.labelToIndex.size + 1
    val numFeatures = flowData.featureToIndex.size

    val trainingData = flowData.data

    //  Empty categoricalFeaturesInfo indicates all features are continuous.
    val categoricalFeaturesInfo = (0 until numFeatures).map(i => i -> 2).toMap

    RandomForest.trainClassifier(trainingData, numClasses, categoricalFeaturesInfo,
      numTrees, featureSubsetStrategy, impurity, maxDepth, maxBins)
  }

}

object RandomForestTrainer {

 def apply() = new RandomForestTrainer

  def main(args: Array[String]) = {

    val conf = new SparkConf(true).setAppName(this.getClass.getSimpleName).
      setMaster("local[*]")

    implicit val sc = new SparkContext(conf)

    val flowData = FlowData.load(Configuration.dataPath)

    val (model, metrics, runtime) = RandomForestTrainer().trainEvaluate(flowData)

    removeDir(Configuration.randomForestPath)
    model.save(Configuration.randomForestPath)

    printEvaluationMetrics(model, metrics)
    println(s"Training for ${model.self.getClass.getSimpleName} was completed in ${runtime/1000} seconds.")


  }

}
