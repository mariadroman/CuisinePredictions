package com.lunatech.webdata.cuisine.mllib

import com.lunatech.webdata._
import com.lunatech.webdata.cuisine.Configuration
import org.apache.spark.mllib.classification.LogisticRegressionWithLBFGS
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object TrainingLogisticRegression {

   val maxDepth = 20
   val maxBins = 32

   def main(args: Array[String]) = {


     val conf = new SparkConf().setAppName("CuisineTrainingLogisticRegression").
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

     trainLogisticRegression(sc, data, numClasses)

   }

   // Train a DecisionTree model with gini impurity.
   def trainLogisticRegression(sc: SparkContext, data: RDD[LabeledPoint], numClasses: Int): Unit = {

     val splits = data.randomSplit(Array(0.8, 0.2))
     val (trainingData, testData) = (splits(0), splits(1))

     val model = new LogisticRegressionWithLBFGS()
       .setNumClasses(numClasses)
       .run(trainingData)

     evaluateModel("LogisticRegression", model, testData)

     removeDir(Configuration.logisticRegPath)
     model.save(sc, Configuration.logisticRegPath)
   }

 }
