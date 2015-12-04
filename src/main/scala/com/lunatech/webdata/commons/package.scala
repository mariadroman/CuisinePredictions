package com.lunatech.webdata

import org.apache.spark.SparkContext
import org.apache.spark.mllib.classification.{LogisticRegressionModel, ClassificationModel}
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.mllib.tree.model.{DecisionTreeModel, RandomForestModel}
import org.apache.spark.rdd.RDD

/**
 *
 */
package object commons {

  trait Predictor {
    def predict(testData: Vector) : Double
    def predict(testData: RDD[Vector]) : RDD[Double]
  }

  trait Model[T] extends Predictor {
    def getModel: T
  }

  object ModelLoader {

    val availableLoaders = Map {
      "logisticregression" -> LogisticRegressionModel
      "logisticregression" -> DecisionTreeModel
    }

    def load(modelType: String, path: String)
               (implicit sc: SparkContext): Model[_] = {
      require(availableLoaders.keySet.contains(modelType.toLowerCase))
      modelType match {
        case "logisticregression" => LogisticRegressionModel.load(sc, path)
        case _ => LogisticRegressionModel.load(sc, path)
      }
    }


  }



  implicit def classificationModelToModel(originalModel: LogisticRegressionModel) =
    new Model[LogisticRegressionModel] {
      def getModel = originalModel
      def predict(testData: Vector): Double = {
        originalModel.predict(testData)
      }
      def predict(testData: RDD[Vector]): RDD[Double] = {
        originalModel.predict(testData)
      }
    }


  implicit def classificationModelToModel[M <: ClassificationModel](originalModel: M) =
    new Model[M] {
      def getModel = originalModel
      def predict(testData: Vector): Double = {
        originalModel.predict(testData)
      }
      def predict(testData: RDD[Vector]): RDD[Double] = {
        originalModel.predict(testData)
      }
    }


  implicit def decisionTreeModelToModel(originalModel: DecisionTreeModel) =
    new Model[DecisionTreeModel] {
      def getModel = originalModel
      def predict(testData: Vector): Double = {
        originalModel.predict(testData)
      }
      def predict(testData: RDD[Vector]): RDD[Double] = {
        originalModel.predict(testData)
      }
    }


  implicit def randomForestModelToModel(originalModel: RandomForestModel) =
    new Model[RandomForestModel] {
      def getModel = originalModel
      def predict(testData: Vector): Double = {
        originalModel.predict(testData)
      }
      def predict(testData: RDD[Vector]): RDD[Double] = {
        originalModel.predict(testData)
      }
    }



}
