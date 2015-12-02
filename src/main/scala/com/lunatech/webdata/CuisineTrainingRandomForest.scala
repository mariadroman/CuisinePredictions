package com.lunatech.webdata

import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.tree.{DecisionTree, RandomForest}
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object CuisineTrainingRandomForest {

  val maxDepth = 30
  val maxBins = 64
  val numTrees = 12
  val featureSubsetStrategy = "auto" // Let the algorithm choose.

  def main(args: Array[String]) = {

    val conf = new SparkConf().setAppName("CuisineTrainingRandomForest").
      setMaster("local[*]").
      set("spark.driver.memory", "16g").
      set("spark.executor.memory", "16g").
      set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")

    val sc = new SparkContext(conf)

    val cuisineToIndex = sc.objectFile[(String, Int)](Configuration.cuisinesPath).collect().toMap
    val ingredientToIndex = sc.objectFile[(String, Int)](Configuration.ingredientsPath).collect().toMap
    val ingredientsIndices = (0 until ingredientToIndex.size)

    val trainingData = MLUtils.loadLabeledPoints(sc, Configuration.dataPath).cache()

    val numClasses = cuisineToIndex.size + 1
    val numFeatures = ingredientsIndices.size

    trainGini(sc, trainingData, numClasses, numFeatures)

    trainEntropy(sc, trainingData, numClasses, numFeatures)

//    trainVariance(sc, trainingData, numClasses, numFeatures)

  }

  // Train a DecisionTree model with gini impurity.
  def trainGini(sc: SparkContext, trainingData: RDD[LabeledPoint], numClasses: Int, numFeatures: Int): Unit = {
    //  Empty categoricalFeaturesInfo indicates all features are continuous.
    val categoricalFeaturesInfo = (0 until numFeatures).map(i => i -> 2).toMap
    val impurity = "gini"

    val model = RandomForest.trainClassifier(trainingData, numClasses, categoricalFeaturesInfo,
      numTrees, featureSubsetStrategy, impurity, maxDepth, maxBins)

    evaluateModel("RandomForest with Gini", model, trainingData)

    removeDir(Configuration.rfGiniPath)
    model.save(sc, Configuration.rfGiniPath)
  }

  // Train a DecisionTree model with entropy impurity
  def trainEntropy(sc: SparkContext, trainingData: RDD[LabeledPoint], numClasses: Int, numFeatures: Int): Unit = {
    //  Empty categoricalFeaturesInfo indicates all features are continuous.
    val categoricalFeaturesInfo = (0 until numFeatures).map(i => i -> 2).toMap
    val impurity = "entropy"

    val model = RandomForest.trainClassifier(trainingData, numClasses, categoricalFeaturesInfo,
      numTrees, featureSubsetStrategy, impurity, maxDepth, maxBins)

    evaluateModel("RandomForest with Entropy", model, trainingData)

    removeDir(Configuration.rfEntropyPath)
    model.save(sc, Configuration.rfEntropyPath)
  }

  // Train a DecisionTree model with variance impurity
  def trainVariance(sc: SparkContext, trainingData: RDD[LabeledPoint], numClasses: Int, numFeatures: Int): Unit = {
    //  Empty categoricalFeaturesInfo indicates all features are continuous.
    val categoricalFeaturesInfo = (0 until numFeatures).map(i => i -> 2).toMap
    val impurity = "variance"

    val model = RandomForest.trainClassifier(trainingData, numClasses, categoricalFeaturesInfo,
      numTrees, featureSubsetStrategy, impurity, maxDepth, maxBins)

    evaluateModel("RandomForest with Variance", model, trainingData)

    removeDir(Configuration.rfVariancePath)
    model.save(sc, Configuration.rfVariancePath)
  }

}
