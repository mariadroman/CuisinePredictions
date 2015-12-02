package com.lunatech.webdata.cuisine.mllib.trainers

import com.lunatech.webdata._
import com.lunatech.webdata.cuisine.Configuration
import com.lunatech.webdata.cuisine.mllib.{FlowData, Model, Trainer}
import org.apache.spark.mllib.tree.DecisionTree
import org.apache.spark.mllib.tree.model.DecisionTreeModel
import org.apache.spark.{SparkConf, SparkContext}

/**
 *
 * @param maxDepth
 * @param maxBins
 * @param impurity acceptable values: "gini" or "entropy"
 */
class DecisionTreeTrainer(maxDepth: Int = 15,
                                maxBins: Int = 1000,
                                impurity: String = "gini")
  extends Trainer[DecisionTreeModel] {


  def train(flowData: FlowData)(implicit sc: SparkContext): Model[DecisionTreeModel] = {

    val numClasses = flowData.labelToIndex.size + 1
    val numFeatures = flowData.featureToIndex.size

    val trainingData = flowData.data

    //  Empty categoricalFeaturesInfo indicates all features are continuous.
    val categoricalFeaturesInfo = (0 until numFeatures).map(i => i -> 2).toMap

    DecisionTree.trainClassifier(trainingData, numClasses, categoricalFeaturesInfo,
      impurity, maxDepth, maxBins)
  }

}

object DecisionTreeTrainer {

  def apply() = new DecisionTreeTrainer

  def main(args: Array[String]) = {

    val conf = new SparkConf(true).setAppName(this.getClass.getSimpleName).
      setMaster("local[*]")

    implicit val sc = new SparkContext(conf)

    val flowData = FlowData.load(Configuration.dataPath)

    val (model, metrics, runtime) = DecisionTreeTrainer().trainEvaluate(flowData)

    removeDir(Configuration.decisionTreePath)
    model.save(Configuration.decisionTreePath)

    printEvaluationMetrics(model, metrics)
    println(s"Training for ${model.self.getClass.getSimpleName} was completed in ${runtime/1000} seconds.")
  }

}
