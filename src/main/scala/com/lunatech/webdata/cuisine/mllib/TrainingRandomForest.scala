package com.lunatech.webdata.cuisine.mllib

import com.lunatech.webdata._
import com.lunatech.webdata.cuisine.Configuration
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.tree.RandomForest
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object TrainingRandomForest {

  val maxDepth = 5
  val maxBins = 100
  val numTrees = 12
  val impurity = "gini" // "entropy"
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

    val data = MLUtils.loadLabeledPoints(sc, Configuration.dataPath).cache()

    val numClasses = cuisineToIndex.size + 1
    val numFeatures = ingredientsIndices.size

    trainGini(sc, data, numClasses, numFeatures)

//    trainEntropy(sc, data, numClasses, numFeatures)

  }

  // Train a DecisionTree model with gini impurity.
  def trainGini(sc: SparkContext, data: RDD[LabeledPoint], numClasses: Int, numFeatures: Int): Unit = {

    val splits = data.randomSplit(Array(0.8, 0.2))
    val (trainingData, testData) = (splits(0), splits(1))

    //  Empty categoricalFeaturesInfo indicates all features are continuous.
    val categoricalFeaturesInfo = (0 until numFeatures).map(i => i -> 2).toMap

    val model = RandomForest.trainClassifier(trainingData, numClasses, categoricalFeaturesInfo,
      numTrees, featureSubsetStrategy, impurity, maxDepth, maxBins)

    evaluateModel(s"RandomForest with $impurity", model, testData)

    removeDir(Configuration.rfGiniPath)
    model.save(sc, Configuration.rfGiniPath)
  }

}
