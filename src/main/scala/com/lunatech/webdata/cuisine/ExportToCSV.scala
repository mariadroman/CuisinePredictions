package com.lunatech.webdata.cuisine

import java.io.{BufferedOutputStream, PrintWriter}

import com.lunatech.webdata.cuisine.mllib.Model
import com.lunatech.webdata.cuisine.model.PredictedRecipe
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.mllib.classification.{LogisticRegressionModel, NaiveBayesModel}
import org.apache.spark.mllib.tree.model.{DecisionTreeModel, RandomForestModel}
import org.apache.spark.{SparkConf, SparkContext}

import scala.util.Try

/**
 *
 */
object ExportToCSV extends SparkRunner {

  def main(args: Array[String]) = {

    implicit val configuration = Configuration(args)

    val defConf = new SparkConf(true)
    val conf = defConf.setAppName("CuisineRecipesExportToCSV").
      setMaster(defConf.get("spark.master",  "local[*]"))

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

    // Export the predictions to ES, one record / recipe
    val predictions = sc.objectFile[PredictedRecipe](configuration.outputPredictionsPath)

    // Create teh HDFS file system handle
    val hdfs = FileSystem.get(new org.apache.hadoop.conf.Configuration())

    // Create an output stream for each model type
    val dos = models.map {m =>
      val path = new Path(s"${configuration.outputPredictionsPath}_${m.name}.csv")
      (m.name, new PrintWriter(new BufferedOutputStream(hdfs.create(path, true)), true))
    }.toMap

    predictions.collect.foreach { pred =>
      pred.predictions.foreach{ p =>
        val os = dos(p.model)
        val record = s"${pred.id}, ${p.prediction}\n"
        Try(os.print(record))
      }
    }

    // Close the streams
    dos.values.foreach(os => Try(os.close()))

  }
}
