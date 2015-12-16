package com.lunatech.webdata.cuisine

import com.lunatech.webdata.cuisine.DaoUtils._
import com.lunatech.webdata.cuisine.mllib.{MulticlassMetrix, ClassMetrics, Model}
import com.lunatech.webdata.cuisine.model.PredictedRecipe
import org.apache.spark.mllib.classification.{LogisticRegressionModel, NaiveBayesModel}
import org.apache.spark.mllib.tree.model.{DecisionTreeModel, RandomForestModel}
import org.apache.spark.{SparkConf, SparkContext}
import org.elasticsearch.spark.rdd.EsSpark

/**
 *
 */
object ExportToES extends SparkRunner {

  def main(args: Array[String]) = {

    implicit val configuration = Configuration(args)

    val defConf = new SparkConf(true)
    val conf = defConf.setAppName("CuisineRecipesExportToES").
      setMaster(defConf.get("spark.master",  "local[*]")).
      set("es.index.auto.create", configuration.es_index_auto_create).
      set("es.nodes", configuration.es_nodes).
      set("es.port", configuration.es_port)

    implicit val sc = new SparkContext(conf)

    run

  }

  def run(implicit sc: SparkContext, configuration: Configuration) = {

    // Export the model metrics to ES
    val models: List[Model[_]] =
      List(
        LogisticRegressionModel.load(sc, configuration.logisticRegPath),
        NaiveBayesModel.load(sc, configuration.naiveBayesPath),
        DecisionTreeModel.load(sc, configuration.decisionTreePath),
        RandomForestModel.load(sc, configuration.randomForestPath)
      )
    case class ModelMetrics(name: String, metrics: MulticlassMetrix)
    val metrics = sc.parallelize(models.map(model => ModelMetrics(model.name, loadMetrix(model).get)))

    EsSpark.saveToEs(metrics,"cuisines/metrics")

    // Export the predictions to ES, one record / recipe
    val predictions = sc.objectFile[PredictedRecipe](configuration.outputPredictionsPath)

    EsSpark.saveToEs(predictions,"cuisines/predictions_raw")

    // Export predictions to ES flattened, one record / recipe / model
    case class Prediction(id: Int, ingredients: Seq[String], model: String, prediction: String, metrics: ClassMetrics)

    val exportPredictions = predictions.flatMap { pred =>
      pred.predictions.map{ p =>
        Prediction(pred.id, pred.ingredients, p.model, p.prediction, p.metrics)
      }
    }

    EsSpark.saveToEs(exportPredictions,"cuisines/predictions")

  }
}
