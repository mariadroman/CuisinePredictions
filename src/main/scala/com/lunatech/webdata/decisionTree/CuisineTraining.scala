package com.lunatech.webdata.decisionTree

import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.tree.DecisionTree
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import com.lunatech.webdata._

object CuisineTraining {

  def main(args: Array[String]) = {


    val conf = new SparkConf().setAppName("CuisineTraining").
      setMaster("local[*]")
    val sc = new SparkContext(conf)

    val cuisineToIndex = sc.objectFile[(String, Int)](Configuration().cuisinesPath).collect().toMap
    val ingredientToIndex = sc.objectFile[(String, Int)](Configuration().ingredientsPath).collect().toMap
    val ingredientsIndices = (0 until ingredientToIndex.size)

    val trainingData = MLUtils.loadLabeledPoints(sc, Configuration().dataPath).cache()

    // Train a DecisionTree model.
    //  Empty categoricalFeaturesInfo indicates all features are continuous.
    val numClasses = cuisineToIndex.size + 1
    val numFeatures = ingredientsIndices.size
//    val categoricalFeaturesInfo = ingredientsIndices.map(i => i -> 2).toMap
//    val impurity = "gini"
//    val maxDepth = 20
//    val maxBins = 32
//
//    val model = DecisionTree.trainClassifier(trainingData, numClasses, categoricalFeaturesInfo,
//      impurity, maxDepth, maxBins)
//
//    removeDir(Configuration().trainingDataPath)
//    model.save(sc, Configuration().trainingDataPath)

    trainGini(sc, trainingData, numClasses, numFeatures)

    trainEntropy(sc, trainingData, numClasses, numFeatures)

    trainVariance(sc, trainingData, numClasses, numFeatures)

  }

  // Train a DecisionTree model with gini impurity.
  def trainGini(sc: SparkContext, trainingData: RDD[LabeledPoint], numClasses: Int, numFeatures: Int): Unit = {
    //  Empty categoricalFeaturesInfo indicates all features are continuous.
    val categoricalFeaturesInfo = (0 to numFeatures).map(i => i -> 2).toMap
    val impurity = "gini"
    val maxDepth = 20
    val maxBins = 32

    val model = DecisionTree.trainClassifier(trainingData, numClasses, categoricalFeaturesInfo,
      impurity, maxDepth, maxBins)

    removeDir(Configuration().giniPath)
    model.save(sc, Configuration().giniPath)
  }

  // Train a DecisionTree model with entropy impurity
  def trainEntropy(sc: SparkContext, trainingData: RDD[LabeledPoint], numClasses: Int, numFeatures: Int): Unit = {
    //  Empty categoricalFeaturesInfo indicates all features are continuous.
    val categoricalFeaturesInfo = (0 to numFeatures).map(i => i -> 2).toMap
    val impurity = "entropy"
    val maxDepth = 20
    val maxBins = 32

    val model = DecisionTree.trainClassifier(trainingData, numClasses, categoricalFeaturesInfo,
      impurity, maxDepth, maxBins)

    removeDir(Configuration().entropyPath)
    model.save(sc, Configuration().entropyPath)
  }

  // Train a DecisionTree model with variance impurity
  def trainVariance(sc: SparkContext, trainingData: RDD[LabeledPoint], numClasses: Int, numFeatures: Int): Unit = {
    //  Empty categoricalFeaturesInfo indicates all features are continuous.
    val categoricalFeaturesInfo = (0 to numFeatures).map(i => i -> 2).toMap
    val impurity = "variance"
    val maxDepth = 20
    val maxBins = 32

    val model = DecisionTree.trainClassifier(trainingData, numClasses, categoricalFeaturesInfo,
      impurity, maxDepth, maxBins)

    removeDir(Configuration().variancePath)
    model.save(sc, Configuration().variancePath)
  }

}
