package com.lunatech.webdata.cuisine.mllib

import com.lunatech.webdata._
import com.lunatech.webdata.cuisine.Configuration
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.tree.DecisionTree
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object TrainingDecisionTrees {

  val maxDepth = 5
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

    val data = MLUtils.loadLabeledPoints(sc, Configuration.dataPath).cache()

    val numClasses = cuisineToIndex.size + 1
    val numFeatures = ingredientsIndices.size

    trainGini(sc, data, numClasses, numFeatures)

//    trainEntropy(sc, data, numClasses, numFeatures)

//    trainVariance(sc, data, numClasses, numFeatures)

  }

  // Train a DecisionTree model with gini impurity.
  def trainGini(sc: SparkContext, data: RDD[LabeledPoint], numClasses: Int, numFeatures: Int): Unit = {

    val splits = data.randomSplit(Array(0.8, 0.2))
    val (trainingData, testData) = (splits(0), splits(1))

    //  Empty categoricalFeaturesInfo indicates all features are continuous.
    val categoricalFeaturesInfo = (0 until numFeatures).map(i => i -> 2).toMap
    val impurity = "gini"

    val model = DecisionTree.trainClassifier(trainingData, numClasses, categoricalFeaturesInfo,
      impurity, maxDepth, maxBins)

    evaluateModel("DecisionTree with Gini", model, testData)

    removeDir(Configuration.dtGiniPath)
    model.save(sc, Configuration.dtGiniPath)
  }

  // Train a DecisionTree model with entropy impurity
  def trainEntropy(sc: SparkContext, data: RDD[LabeledPoint], numClasses: Int, numFeatures: Int): Unit = {

    val splits = data.randomSplit(Array(0.8, 0.2))
    val (trainingData, testData) = (splits(0), splits(1))

    //  Empty categoricalFeaturesInfo indicates all features are continuous.
    val categoricalFeaturesInfo = (0 until numFeatures).map(i => i -> 2).toMap
    val impurity = "entropy"

    val model = DecisionTree.trainClassifier(trainingData, numClasses, categoricalFeaturesInfo,
      impurity, maxDepth, maxBins)

    evaluateModel("DecisionTree with Entropy", model, testData)

    removeDir(Configuration.dtEntropyPath)
    model.save(sc, Configuration.dtEntropyPath)
  }

  // Train a DecisionTree model with variance impurity
  def trainVariance(sc: SparkContext, data: RDD[LabeledPoint], numClasses: Int, numFeatures: Int): Unit = {

    val splits = data.randomSplit(Array(0.8, 0.2))
    val (trainingData, testData) = (splits(0), splits(1))

    //  Empty categoricalFeaturesInfo indicates all features are continuous.
    val categoricalFeaturesInfo = (0 until numFeatures).map(i => i -> 2).toMap
    val impurity = "variance"

    val model = DecisionTree.trainClassifier(trainingData, numClasses, categoricalFeaturesInfo,
      impurity, maxDepth, maxBins)

    evaluateModel("DecisionTree with Variance", model, testData)

    removeDir(Configuration.dtVariancePath)
    model.save(sc, Configuration.dtVariancePath)
  }

}
