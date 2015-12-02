package com.lunatech.webdata

import org.apache.spark.mllib.classification.NaiveBayes
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object CuisineTrainingNaiveBayes {

   val maxDepth = 30
   val maxBins = 64

   def main(args: Array[String]) = {


     val conf = new SparkConf().setAppName("CuisineTrainingNaiveBayes").
       setMaster("local[*]").
       set("spark.driver.memory", "16g").
       set("spark.executor.memory", "16g").
       set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")

     val sc = new SparkContext(conf)

     val cuisineToIndex = sc.objectFile[(String, Int)](Configuration.cuisinesPath).collect().toMap
     val ingredientToIndex = sc.objectFile[(String, Int)](Configuration.ingredientsPath).collect().toMap
     val ingredientsIndices = (0 until ingredientToIndex.size)

     val trainingData = MLUtils.loadLabeledPoints(sc, Configuration.dataPath).cache()

     trainNaiveBayes(sc, trainingData)

   }

   // Train a DecisionTree model with gini impurity.
   def trainNaiveBayes(sc: SparkContext, trainingData: RDD[LabeledPoint]): Unit = {

     val model = NaiveBayes.train(trainingData, lambda = 1.0, modelType = "multinomial")

     evaluateModel("NaiveBayes", model, trainingData)

     removeDir(Configuration.naiveBayesPath)
     model.save(sc, Configuration.naiveBayesPath)
   }

 }
