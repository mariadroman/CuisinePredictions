package com.lunatech.webdata.cuisine.mllib

import com.lunatech.webdata._
import com.lunatech.webdata.cuisine.Configuration
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.tree.DecisionTree
import org.apache.spark.mllib.tree.model.DecisionTreeModel
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import com.lunatech.webdata.commons._

object TrainingDecisionTrees extends Trainer[DecisionTreeModel]{

  val maxDepth = 5
  val maxBins = 32
  val impurity = "gini" // "entropy"

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

    val params = Map("numClasses" -> numClasses, "numFeatures" -> numFeatures)

    train(sc, data, numClasses, numFeatures)

//    trainEntropy(sc, data, numClasses, numFeatures)

  }

  def train(data: RDD[LabeledPoint], params: Map[String, String])(implicit sc: SparkContext) = {
    val numClasses = params("numClasses").toInt
    val numFeatures = params("numFeatures").toInt


    //  Empty categoricalFeaturesInfo indicates all features are continuous.
    val categoricalFeaturesInfo = (0 until numFeatures).map(i => i -> 2).toMap

    DecisionTree.trainClassifier(data, numClasses, categoricalFeaturesInfo,
      impurity, maxDepth, maxBins)


  }

  // Train a DecisionTree model with gini impurity.
  def train(sc: SparkContext, data: RDD[LabeledPoint], numClasses: Int, numFeatures: Int): DecisionTreeModel = {

    val splits = data.randomSplit(Array(0.8, 0.2))
    val (trainingData, testData) = (splits(0), splits(1))

    //  Empty categoricalFeaturesInfo indicates all features are continuous.
    val categoricalFeaturesInfo = (0 until numFeatures).map(i => i -> 2).toMap

    val model = DecisionTree.trainClassifier(trainingData, numClasses, categoricalFeaturesInfo,
      impurity, maxDepth, maxBins)

    evaluateModel(s"DecisionTree with $impurity", model, testData)

    removeDir(Configuration.dtGiniPath)
    model.save(sc, Configuration.dtGiniPath)

    model
  }

}
