package com.lunatech.webdata.cuisine

import com.lunatech.webdata.cuisine.mllib.{FlowData, Model}
import com.lunatech.webdata.cuisine.model.{PredictionData, PredictedRecipe}
import org.apache.spark.mllib.classification.{NaiveBayesModel, LogisticRegressionModel}
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.tree.model.{RandomForestModel, DecisionTreeModel}
import org.apache.spark.{SparkConf, SparkContext}

/**
 * An example of following through the process from data import to predictions.
 *
 * The example is mainly designed for multi class classification with discrete features
 *
 */
object BuildPredictions extends SparkRunner {

  def main(args: Array[String]) = {

    val conf = new SparkConf().setAppName("CuisineRecipesBuildPredictions").
      setMaster("local[*]")

    implicit val sc = new SparkContext(conf)

    run

  }

  def run(implicit sc: SparkContext) = {

    import DaoUtils._

    // Load the flow data
    val flowData = FlowData.load(Configuration.dataPath)

    // Import the test recipes for predictions
    val testRecipes = RecipesImporter.importFrom(Configuration.inputTestingData)

    // Transform the test recipes into feature vectors
    val testData = testRecipes.map { r =>
      // Hmm... we have some new ingredients... should we ignore them? For now yes.
      val filteredIngredients = r.ingredients.filter(flowData.featureToIndex.contains(_))
      val values = filteredIngredients.map(i => 1.0).toArray
      val indices = filteredIngredients.map(flowData.featureToIndex).sorted.toArray
      val vector = Vectors.sparse(flowData.featureToIndex.size, indices, values)
      (r.id, vector)
    }.cache

    // Set the models are we using for predictions
    val models: List[Model[_]] =
      List(
        LogisticRegressionModel.load(sc, Configuration.logisticRegPath)
        ,
        NaiveBayesModel.load(sc, Configuration.naiveBayesPath),
        DecisionTreeModel.load(sc, Configuration.decisionTreePath),
        RandomForestModel.load(sc, Configuration.randomForestPath)
      )

    val metrics = models.map(model => (model.name -> loadMetrix(model))).toMap

    // Prepare the data to be predicted
    val predictionData = testData.map { case (recipeId, featuresVector) =>
      val predictions = models.map { model =>
        val prediction = model.predict(featuresVector)
        (model.name -> prediction)
      }.toMap
      (recipeId, predictions, featuresVector)
    }

    // Predict the recipes
    val predictedRecipes = predictionData.map {
      case (recipeId, predictions, featuresVector) =>
      val ingredientsIndices = featuresVector.toSparse.indices
      val ingredients = ingredientsIndices.map(flowData.indexToFeature(_)).toSeq
      val predictionData = predictions.map{p =>
        val modelName = p._1
        val predictedCuisine = flowData.indexToLabel(p._2.toInt)
        val classMetrics = metrics(modelName).get.metricsByLabel(predictedCuisine)
        PredictionData(modelName, predictedCuisine, classMetrics)
      }.toSeq //HR as in Human Readable

      PredictedRecipe(recipeId, ingredients, predictionData)
    }


    // Print some results
    predictedRecipes.take(20).foreach { p =>

      println(
        s"Recipe Id: ${p.id}")
      println("  Ingredients:")
      p.ingredients.foreach(i => println(s"  - ${i}"))
      println("  Predictions and class specific metrics:")
      println(f"  | ${"Model Name"}%-30s | ${"Prediction"}%-30s | ${"Prec/cls"}%-8s | ${"TPR/cls"}%-8s | ${"FPR/cls"}%-8s")
      p.predictions.foreach ( p =>
        println(f"  | ${p.model}%-30s | ${p.prediction}%-30s | ${p.metrics.precision * 100}%7.4f%% | ${p.metrics.truePositiveRate * 100}%7.4f%% | ${p.metrics.falsePositiveRate * 100}%7.4f%%")
      )

    }

  }

}
