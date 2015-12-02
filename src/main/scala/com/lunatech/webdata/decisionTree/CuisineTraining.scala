package com.lunatech.webdata.decisionTree

import org.apache.spark.mllib.tree.DecisionTree
import org.apache.spark.mllib.util.MLUtils
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

    val trainingData = MLUtils.loadLabeledPoints(sc, Configuration().dataPath)

    // Train a DecisionTree model.
    //  Empty categoricalFeaturesInfo indicates all features are continuous.
    val numClasses = cuisineToIndex.size + 1
    val categoricalFeaturesInfo = ingredientsIndices.map(i => i -> 2).toMap
    val impurity = "gini"
    val maxDepth = 20
    val maxBins = 32

    val model = DecisionTree.trainClassifier(trainingData, numClasses, categoricalFeaturesInfo,
      impurity, maxDepth, maxBins)

    removeDir(Configuration().trainingDataPath)
    model.save(sc, Configuration().trainingDataPath)

  }

}
