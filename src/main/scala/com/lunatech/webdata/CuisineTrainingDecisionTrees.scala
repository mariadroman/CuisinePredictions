package com.lunatech.webdata

import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.tree.{RandomForest, DecisionTree}
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object CuisineTrainingDecisionTrees {

  val maxDepth = 20
  val maxBins = 32

  def main(args: Array[String]) = {


    val conf = new SparkConf().setAppName("CuisineTrainingDecisionTrees").
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

    val model = DecisionTree.trainClassifier(trainingData, numClasses, categoricalFeaturesInfo,
      impurity, maxDepth, maxBins)

    evaluateModel("DecisionTree with Gini", model, trainingData)

    removeDir(Configuration.dtGiniPath)
    model.save(sc, Configuration.dtGiniPath)
  }

  // Train a DecisionTree model with entropy impurity
  def trainEntropy(sc: SparkContext, trainingData: RDD[LabeledPoint], numClasses: Int, numFeatures: Int): Unit = {
    //  Empty categoricalFeaturesInfo indicates all features are continuous.
    val categoricalFeaturesInfo = (0 until numFeatures).map(i => i -> 2).toMap
    val impurity = "entropy"

    val model = DecisionTree.trainClassifier(trainingData, numClasses, categoricalFeaturesInfo,
      impurity, maxDepth, maxBins)

    evaluateModel("DecisionTree with Entropy", model, trainingData)

    removeDir(Configuration.dtEntropyPath)
    model.save(sc, Configuration.dtEntropyPath)
  }

  // Train a DecisionTree model with variance impurity
  def trainVariance(sc: SparkContext, trainingData: RDD[LabeledPoint], numClasses: Int, numFeatures: Int): Unit = {
    //  Empty categoricalFeaturesInfo indicates all features are continuous.
    val categoricalFeaturesInfo = (0 until numFeatures).map(i => i -> 2).toMap
    val impurity = "variance"

    val model = DecisionTree.trainClassifier(trainingData, numClasses, categoricalFeaturesInfo,
      impurity, maxDepth, maxBins)

    evaluateModel("DecisionTree with Variance", model, trainingData)

    removeDir(Configuration.dtVariancePath)
    model.save(sc, Configuration.dtVariancePath)
  }

}
