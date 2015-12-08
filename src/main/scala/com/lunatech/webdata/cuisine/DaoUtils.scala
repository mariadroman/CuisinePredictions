package com.lunatech.webdata.cuisine

import java.io.{FileInputStream, ObjectInputStream, FileOutputStream, ObjectOutputStream}

import com.lunatech.webdata._
import com.lunatech.webdata.cuisine.mllib.{MulticlassMetrix, Model}
import org.apache.spark.SparkContext
import org.apache.spark.mllib.classification.{NaiveBayesModel, LogisticRegressionModel}
import org.apache.spark.mllib.tree.model.{RandomForestModel, DecisionTreeModel}

import scala.util.Try

/**
 * VERY UGLY, but for now it works.
 */
object DaoUtils {


  def saveModel(model: Model[_])(implicit sc: SparkContext) = {
    val path = getPath(model)
    removeDir(path)
    model.save(path)
  }

  def saveMetrix(model: Model[_], metrics: MulticlassMetrix)(implicit sc: SparkContext) = {
    val path = getPath(model) + ".metrics"
    Try(new java.io.File(path).delete())
    Try(new ObjectOutputStream(new FileOutputStream(path)))
      .foreach{p =>
        p.writeObject(metrics); p.close
      }
  }
  def loadMetrix(model: Model[_])(implicit sc: SparkContext): Try[MulticlassMetrix] = {
    val path = getPath(model) + ".metrics"

    Try(new ObjectInputStream(new FileInputStream(path)))
      .map{ p =>
        val metrics = p.readObject().asInstanceOf[MulticlassMetrix]
        p.close
        metrics
      }
  }

  def getPath(model: Model[_]) = model.self match {
    case m1: LogisticRegressionModel => Configuration.logisticRegPath
    case m2: NaiveBayesModel => Configuration.naiveBayesPath
    case m2: DecisionTreeModel => Configuration.decisionTreePath
    case m2: RandomForestModel => Configuration.randomForestPath
  }

}
