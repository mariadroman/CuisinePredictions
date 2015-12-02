package com.lunatech.webdata.cuisine

import com.lunatech.webdata._
import com.lunatech.webdata.cuisine.mllib.trainers.LogisticRegressionTrainer
import com.lunatech.webdata.cuisine.mllib.{Model, Trainer, TrainingDataImporter}
import org.apache.spark.mllib.classification.{LogisticRegressionModel, NaiveBayesModel}
import org.apache.spark.mllib.evaluation.MulticlassMetrics
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.tree.model.{DecisionTreeModel, RandomForestModel}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * An example of following through the process from data import to predictions.
  *
  * The example is mainly designed for multi class classification with discrete features
  *
  */
object MainMLib {

  def main(args: Array[String]) = {

    val conf = new SparkConf().setAppName("CuisineRecipesPrediction").
      setMaster("local[*]")

    implicit val sc = new SparkContext(conf)

    // Import the data and process it
    val flowData = TrainingDataImporter.importFrom(Configuration.inputTrainingData)

    // Store the flow data for later processing
    flowData.save(Configuration.dataPath)

    def train[T](trainer: Trainer[T]): (Model[T], MulticlassMetrics, Trainer.Runtime) =
      trainer.trainEvaluate(flowData)

    // Train a model
    val (model, metrics, runtime) = train(LogisticRegressionTrainer())

    // Save the model for later use
    saveModel(model)

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

    printEvaluationMetrics(model, metrics)

  }

  def saveModel(model: Model[_])(implicit sc: SparkContext) = {
    removeDir(getPath(model))
    model.save(getPath(model))
  }

  def getPath(model: Model[_]) = model.self match {
    case m1: LogisticRegressionModel => Configuration.logisticRegPath
    case m2: NaiveBayesModel => Configuration.naiveBayesPath
    case m2: DecisionTreeModel => Configuration.decisionTreePath
    case m2: RandomForestModel => Configuration.randomForestPath
  }
}
