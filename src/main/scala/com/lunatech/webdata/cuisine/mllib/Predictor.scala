package com.lunatech.webdata.cuisine.mllib

import com.lunatech.webdata.cuisine.{Configuration, RecipesImporter}
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.tree.model.DecisionTreeModel
import org.apache.spark.{SparkConf, SparkContext}

/**
 *
 */
object Predictor {


  def main(args: Array[String]) = {

    val conf = new SparkConf().setAppName("CuisineRecipesPrediction").
      setMaster("local[*]")

    implicit val sc = new SparkContext(conf)

    val flowData = FlowData.load(Configuration.dataPath)

    // Import the test recipes for predictions
    val testRecipes = RecipesImporter.importFrom(Configuration.inputTestingData)

    // Transform the test recipes into feature vectors
    val testData = testRecipes.map { r =>
      // Hmm... we have some new ingredients... should we ignore them? For now yes.
      val filteredIngredients = r.ingredients.filter(flowData.featureToIndex.contains(_))
      val values = filteredIngredients.map(i => 1.0).toArray
      val indices = filteredIngredients.map(flowData.featureToIndex(_)).sorted.toArray
      val vector = Vectors.sparse(flowData.featureToIndex.size, indices, values)
      (r.id, vector)
    }.cache

    val model = DecisionTreeModel.load(sc, Configuration.decisionTreePath)

    // Evaluate model on test instances and compute test error
    val predictionData = testData.map { pk =>
      val prediction = model.predict(pk._2)
      (pk._1, prediction, pk._2)
    }

    predictionData.takeSample(false, 30).foreach { r =>
      println("--------------------------------------------------")
      val recipeId = r._1.toInt
      val predictedCuisine = flowData.indexToLabel(r._2.toInt)
      val ingredientsIndices = r._3.toSparse.indices
      println(
        s"Recipe Id: $recipeId | " +
          s"Predicted: ${predictedCuisine.toUpperCase()} ")
      println("  Ingredients:")
      ingredientsIndices.foreach(i => println(s"  - ${flowData.indexToFeature(i)}"))

    }

  }

}
