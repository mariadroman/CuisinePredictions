package com.lunatech.webdata.decisionTree

import com.lunatech.webdata.{CustomLineInputFormat, ImportDataModel}
import org.apache.hadoop.io.{Text, LongWritable}
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.tree.model.DecisionTreeModel
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object CuisinePredictions {

  val testDataPath: String = "ML/train.json"

  def main(args: Array[String]) = {

    val conf = new SparkConf().setAppName("CuisinePredictions").
      setMaster("local[*]")
    val sc = new SparkContext(conf)

    val rawData: RDD[(LongWritable, Text)] =
      sc.newAPIHadoopFile[LongWritable, Text, CustomLineInputFormat](testDataPath)
    val testRecipes = ImportDataModel.rawDataToRecipes(rawData)

    val cuisineToIndex = sc.objectFile[(String, Int)](Configuration().cuisinesPath).
      collect.toMap
    val ingredientToIndex = sc.objectFile[(String, Int)](Configuration().ingredientsPath).
      collect.toMap

    val cuisinesNames = cuisineToIndex.map(r => r._2 -> r._1)
    val ingredientsNames = ingredientToIndex.map(r => r._2 -> r._1)

    val data = MLUtils.loadLabeledPoints(sc, Configuration().dataPath)

    val testData = testRecipes.map { r =>
      val label = cuisineToIndex(r.cuisine)
      val values = r.ingredients.map(i => 1.0).toArray
      val indices = r.ingredients.map(ingredientToIndex(_)).toArray
      val vector = Vectors.sparse(ingredientToIndex.size, indices, values)
      r.id -> LabeledPoint(label, vector)
    }

    val model = DecisionTreeModel.load(sc, Configuration().trainingDataPath)

    // Evaluate model on test instances and compute test error
    val labelAndPreds = testData.map { pk =>
      val prediction = model.predict(pk._2.features)
      (pk._1, pk._2.label, prediction, pk._2.features)
    }

    println("--------------------------------------------------")
    val testErr = labelAndPreds.filter(r => r._2 != r._3).count.toDouble / testData.count()
    println(f"Test Error = ${testErr * 100}%.2f")

    val testMSE = labelAndPreds.map{ r => math.pow((r._2 - r._3), 2)}.mean()
    println(s"Test Mean Squared Error = $testMSE")

//    println("--------------------------------------------------")
//    labelAndPreds.takeSample(false, 100).foreach(r => println(
//      s"Actual: ${r._2} | Predicted: ${r._3}"))

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

  }

}


