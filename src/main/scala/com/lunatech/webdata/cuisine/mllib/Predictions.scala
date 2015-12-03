package com.lunatech.webdata.cuisine.mllib

import com.lunatech.webdata._
import com.lunatech.webdata.cuisine.{Configuration, ImportDataModel}
import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.spark.mllib.classification.{ClassificationModel, LogisticRegressionModel, NaiveBayesModel}
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.tree.model.{DecisionTreeModel, RandomForestModel}
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Predictions {

  val dataPath: String = Configuration.inputTestingData

  def main(args: Array[String]) = {

    val conf = new SparkConf(true).setAppName("CuisinePredictions").
      setMaster("local[*]").
      set("spark.driver.memory", "8g").
      set("spark.executor.memory", "8g").
      set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")

    conf.getAll.foreach(println)

    val sc = new SparkContext(conf)

    val rawData: RDD[(LongWritable, Text)] =
      sc.newAPIHadoopFile[LongWritable, Text, CustomLineInputFormat](dataPath)
    val testRecipes = ImportDataModel.rawDataToRecipes(rawData)

    val cuisineToIndex = sc.objectFile[(String, Int)](Configuration.cuisinesPath).
      collect.toMap
    val ingredientToIndex = sc.objectFile[(String, Int)](Configuration.ingredientsPath).
      collect.toMap

    val cuisinesNames = cuisineToIndex.map(r => r._2 -> r._1)
    val ingredientsNames = ingredientToIndex.map(r => r._2 -> r._1)

    val predictData = testRecipes.map { r =>
      // Hmm... we have some new ingredients... should we ignore them? For now yes.
      val filteredIngredients = r.ingredients.filter(ingredientToIndex.keySet.contains(_))
      val values = filteredIngredients.map(i => 1.0).toArray
      val indices = filteredIngredients.map(ingredientToIndex.getOrElse(_, 0)).toArray
      val vector = Vectors.sparse(ingredientToIndex.size, indices, values)
      (r.id, vector)
    }

    val validationData = MLUtils.loadLabeledPoints(sc, Configuration.dataPath).cache()

    def loadClassModel(modelType: String): ClassificationModel = modelType match {
      case "logisticRegression" => LogisticRegressionModel.load(sc, Configuration.logisticRegPath)
      case "naiveBayes" => NaiveBayesModel.load(sc, Configuration.naiveBayesPath)
    }

    def loadTreeModel(modelType: String): DecisionTreeModel = modelType match {
      case "entropy" => DecisionTreeModel.load(sc, Configuration.dtEntropyPath)
      case "gini" => DecisionTreeModel.load(sc, Configuration.dtGiniPath)
    }

    def loadForestModel(modelType: String): RandomForestModel = modelType match {
      case "entropy" => RandomForestModel.load(sc, Configuration.rfEntropyPath)
      case "gini" => RandomForestModel.load(sc, Configuration.rfGiniPath)
    }

    val model = loadClassModel("logisticRegression")
    // val model = loadTreeModel("gini")

    // Evaluate model on test instances and compute test error
    val predictions = predictData.map { pk =>
      val prediction = model.predict(pk._2)
      (pk._1,prediction, pk._2)
    }

    predictions.takeSample(false, 30).foreach{ r =>
      println("--------------------------------------------------")
      val recipeId = r._1.toInt
      val predictedCuisine = cuisinesNames(r._2.toInt)
      val ingredientsIndices = r._3.toSparse.indices.map(_.toInt)
      println(
        s"Recipe Id: $recipeId | " +
          s"Predicted: ${predictedCuisine.toUpperCase()} ")
      println("  Ingredients:")
      ingredientsIndices.foreach(i => println(s"  - ${ingredientsNames(i)}"))

    }


    evaluateModel("Test against all Training data", model, validationData)

  }

}


