package com.lunatech.webdata

import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.spark.mllib.classification.{ClassificationModel, NaiveBayesModel, LogisticRegressionModel}
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.tree.model.{RandomForestModel, DecisionTreeModel}
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object CuisinePredictions {

  val testDataPath: String = "ML/train.json"

  def main(args: Array[String]) = {

    val conf = new SparkConf().setAppName("CuisinePredictions").
      setMaster("local[*]").
      set("spark.driver.memory", "16g").
      set("spark.executor.memory", "16g").
      set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")

    val sc = new SparkContext(conf)

    val rawData: RDD[(LongWritable, Text)] =
      sc.newAPIHadoopFile[LongWritable, Text, CustomLineInputFormat](testDataPath)
    val testRecipes = ImportDataModel.rawDataToRecipes(rawData)

    val cuisineToIndex = sc.objectFile[(String, Int)](Configuration.cuisinesPath).
      collect.toMap
    val ingredientToIndex = sc.objectFile[(String, Int)](Configuration.ingredientsPath).
      collect.toMap

    val cuisinesNames = cuisineToIndex.map(r => r._2 -> r._1)
    val ingredientsNames = ingredientToIndex.map(r => r._2 -> r._1)

    val data = MLUtils.loadLabeledPoints(sc, Configuration.dataPath)

    val testData = testRecipes.map { r =>
      val label = cuisineToIndex(r.cuisine)
      val values = r.ingredients.map(i => 1.0).toArray
      val indices = r.ingredients.map(ingredientToIndex(_)).toArray
      val vector = Vectors.sparse(ingredientToIndex.size, indices, values)
      r.id -> LabeledPoint(label, vector)
    }

    def loadClassModel(modelType: String): ClassificationModel = modelType match {
      case "logisticRegression" => LogisticRegressionModel.load(sc, Configuration.logisticRegPath)
      case "naiveBayes" => NaiveBayesModel.load(sc, Configuration.naiveBayesPath)
    }

    def loadTreeModel(modelType: String): DecisionTreeModel = modelType match {
      case "dtEntropy" => DecisionTreeModel.load(sc, Configuration.dtEntropyPath)
      case "dtGini" => DecisionTreeModel.load(sc, Configuration.dtGiniPath)
    }

    def loadForestModel(modelType: String): RandomForestModel = modelType match {
      case "rfEntropy" => RandomForestModel.load(sc, Configuration.rfEntropyPath)
      case "rfGini" => RandomForestModel.load(sc, Configuration.rfGiniPath)
    }

    val model = loadClassModel("logisticRegression")

    // Evaluate model on test instances and compute test error
    val labelAndPreds = testData.map { pk =>
      val prediction = model.predict(pk._2.features)
      (pk._1, pk._2.label, prediction, pk._2.features)
    }

    val error = labelAndPreds.filter(r => r._2 != r._3).count.toDouble / testData.count()

    val accuracy = labelAndPreds.filter(r => r._2 == r._3).count.toDouble / testData.count()

    val testMSE = labelAndPreds.map{ r => math.pow((r._2 - r._3), 2)}.mean()


    labelAndPreds.takeSample(false, 20).foreach{ r =>
      println("--------------------------------------------------")
      val actualCuisine = cuisinesNames(r._2.toInt)
      val predictedCuisine = cuisinesNames(r._3.toInt)
      val ingredientsIndices = r._4.toSparse.indices.map(_.toInt)
      println(
        s"Actual: ${actualCuisine.toUpperCase()} [${r._1}] | " +
          s"Predicted: ${predictedCuisine.toUpperCase()} ")
      println("  Ingredients:")
      ingredientsIndices.foreach(i => println(s"  - ${ingredientsNames(i)}"))

    }
    println("--------------------------------------------------")
    println(f"Accuracy = ${accuracy * 100}%.2f%%")
    println(f"Error    = ${error * 100}%.2f%%")
    println(s"Test Mean Squared Error = $testMSE")

  }

}


