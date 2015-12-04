package com.lunatech.webdata.cuisine.mllib

import com.lunatech.webdata._
import com.lunatech.webdata.cuisine.Configuration
import org.apache.spark.mllib.classification.NaiveBayes
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object TrainingNaiveBayes {

   val maxDepth = 30
   val maxBins = 64
  val modelType = "multinomial" // "bernoulli"

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

     val data = MLUtils.loadLabeledPoints(sc, Configuration.dataPath).cache()

     trainNaiveBayes(sc, data)

   }

   // Train a DecisionTree model with gini impurity.
   def trainNaiveBayes(sc: SparkContext, data: RDD[LabeledPoint]): Unit = {

     val splits = data.randomSplit(Array(0.8, 0.2))
     val (trainingData, testData) = (splits(0), splits(1))

     val model = NaiveBayes.train(trainingData, lambda = 1.0, modelType)

     evaluateModel("NaiveBayes", model, testData)

     removeDir(Configuration.naiveBayesPath)
     model.save(sc, Configuration.naiveBayesPath)
   }

 }
