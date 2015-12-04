package com.lunatech.webdata.commons

import org.apache.spark.mllib.classification.ClassificationModel
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.mllib.tree.model.{RandomForestModel, DecisionTreeModel}
import org.apache.spark.rdd.RDD
/**
 *
 */
trait Predictor[T] {
  def predict(model: T, testData: Vector): Double
  def predict(model: T, testData: RDD[Vector]): RDD[Double]
}

object Predictor {

  implicit object ClassificationModelPredictor extends Predictor[ClassificationModel] {
    def predict(model: ClassificationModel, testData: Vector): Double = {
      model.predict(testData)
    }
    def predict(model: ClassificationModel, testData: RDD[Vector]): RDD[Double] = {
      model.predict(testData)
    }
  }

  implicit object DecisionTreeModelPredictor extends Predictor[DecisionTreeModel] {
    def predict(model: DecisionTreeModel, testData: Vector): Double = {
      model.predict(testData)
    }
    def predict(model: DecisionTreeModel, testData: RDD[Vector]): RDD[Double] = {
      model.predict(testData)
    }
  }

  implicit object RandomForestModelPredictor extends Predictor[RandomForestModel] {
    def predict(model: RandomForestModel, testData: Vector): Double = {
      model.predict(testData)
    }
    def predict(model: RandomForestModel, testData: RDD[Vector]): RDD[Double] = {
      model.predict(testData)
    }
  }

}

