package com.lunatech

import org.apache.commons.io.FileUtils
import org.apache.spark.mllib.classification.ClassificationModel
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.tree.model.{GradientBoostedTreesModel, DecisionTreeModel, RandomForestModel}
import org.apache.spark.rdd.RDD

/**
 *
 */
package object webdata {

  def removeDir(path: String) = {
    val dir = new java.io.File(path)
    if(dir exists)
      FileUtils.deleteDirectory(dir)
  }

  def evaluateModel(name: String, model: RandomForestModel, testData: RDD[LabeledPoint]) = {
    // Evaluate model on test instances and compute test error
    val labelsAndPreditions = testData.map { point =>
      val prediction = model.predict(point.features)
      (point.label, prediction)
    }
    printModelEvaluation(name, labelsAndPreditions)
  }

  def evaluateModel(name: String, model: DecisionTreeModel, testData: RDD[LabeledPoint]) = {
    // Evaluate model on test instances and compute test error
    val labelsAndPreditions = testData.map { point =>
      val prediction = model.predict(point.features)
      (point.label, prediction)
    }
    printModelEvaluation(name, labelsAndPreditions)
  }

  def evaluateModel(name: String, model: ClassificationModel, testData: RDD[LabeledPoint]) = {
    // Evaluate model on test instances and compute test error
    val labelsAndPreditions = testData.map { point =>
      val prediction = model.predict(point.features)
      (point.label, prediction)
    }
    printModelEvaluation(name, labelsAndPreditions)
  }

  def evaluateModel(name: String, model: GradientBoostedTreesModel, testData: RDD[LabeledPoint]) = {
    // Evaluate model on test instances and compute test error
    val labelsAndPreditions = testData.map { point =>
      val prediction = model.predict(point.features)
      (point.label, prediction)
    }
    printModelEvaluation(name, labelsAndPreditions)
  }



    private def printModelEvaluation(name: String, labelsAndPreditions: RDD[(Double, Double)]) = {

    val accuracy = labelsAndPreditions.filter(r => r._1 == r._2).count.toDouble / labelsAndPreditions.count()
    val testMSE = labelsAndPreditions.map{ r => math.pow((r._1 - r._2), 2)}.mean()

    println(s"$name model evaluation")
    println("---------------------------------")
    println(f"Accuracy = ${accuracy * 100}%.4f%%")
    println(f"Error    = ${(1 - accuracy) * 100}%.4f%%")
    println(f"Test Mean Squared Error = $testMSE%.4f")

  }

}
