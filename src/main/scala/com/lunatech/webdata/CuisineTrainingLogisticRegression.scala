package com.lunatech.webdata

import org.apache.spark.mllib.classification.{LogisticRegressionWithLBFGS, SVMWithSGD, NaiveBayes}
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object CuisineTrainingLogisticRegression {

   val maxDepth = 30
   val maxBins = 64

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

     val trainingData = MLUtils.loadLabeledPoints(sc, Configuration.dataPath).cache()

     val numClasses = cuisineToIndex.size + 1

     trainLogisticRegression(sc, trainingData, numClasses)

   }

   // Train a DecisionTree model with gini impurity.
   def trainLogisticRegression(sc: SparkContext, trainingData: RDD[LabeledPoint], numClasses: Int): Unit = {

     val model = new LogisticRegressionWithLBFGS()
       .setNumClasses(numClasses)
       .run(trainingData)

     removeDir(Configuration.logisticRegPath)
     model.save(sc, Configuration.logisticRegPath)
   }

 }
