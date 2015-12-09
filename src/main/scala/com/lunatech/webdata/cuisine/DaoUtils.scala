package com.lunatech.webdata.cuisine

import java.io._

import com.lunatech.webdata._
import com.lunatech.webdata.cuisine.mllib.{Model, MulticlassMetrix}
import org.apache.spark.SparkContext
import org.apache.spark.mllib.classification.{LogisticRegressionModel, NaiveBayesModel}
import org.apache.spark.mllib.tree.model.{DecisionTreeModel, RandomForestModel}

import scala.reflect.Manifest
import scala.util.Try

/**
 * VERY UGLY, but for now it works.
 */
object DaoUtils {

  def saveModel(model: Model[_])(implicit sc: SparkContext, configuration: Configuration) = {
    val path = getPath(model)
    removeFile(path)
    model.save(path)
  }

  def saveMetrix(model: Model[_], metrics: MulticlassMetrix)(implicit sc: SparkContext, configuration: Configuration) = {
    val path = getPath(model) + ".metrics"
    removeFile(path)
    toJsonFile(metrics, path)
  }
  def loadMetrix[T](model: Model[_])(implicit sc: SparkContext, mf: Manifest[T], configuration: Configuration):
      Try[MulticlassMetrix] = {
    val path = getPath(model) + ".metrics"
    fromJsonFile[MulticlassMetrix](path)
  }

  def getPath(model: Model[_])(implicit configuration: Configuration) = model.self match {
    case m1: LogisticRegressionModel => configuration.logisticRegPath
    case m2: NaiveBayesModel => configuration.naiveBayesPath
    case m2: DecisionTreeModel => configuration.decisionTreePath
    case m2: RandomForestModel => configuration.randomForestPath
  }

  def toJsonFile[T <: scala.AnyRef](that: T, path: String): Boolean = {

    import org.json4s._
    import org.json4s.jackson.Serialization
    import org.json4s.jackson.Serialization._
    implicit val formats = Serialization.formats(NoTypeHints)
    val writer = new BufferedWriter(new FileWriter(path, true))
    val result = Try(write(that, writer)).isSuccess
    Try(writer.close)
    result
  }


  def fromJsonFile[T](path: String)(implicit mf: Manifest[T]): Try[T] = {

    import org.json4s._
    import org.json4s.jackson.Serialization
    import org.json4s.jackson.Serialization._
    implicit val formats = Serialization.formats(NoTypeHints)
    val reader = new FileReader(path)
    val result = Try(read[T](reader))
    Try(reader.close)
    result
  }


}
