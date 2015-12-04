package com.lunatech.webdata.commons

import org.apache.spark.SparkContext
import org.apache.spark.mllib.classification.LogisticRegressionModel

/**
 *
 */
trait ModelLoader[T] {
  def load(path: String)(implicit sc: SparkContext): T
}

object ModelLoader {


  val availableLoaders = Map {
    "logisticregression" -> LogisticRegressionModel
  }

  def load[T](modelType: String, path: String)
                              (implicit sc: SparkContext): Model[T] = {
    require(availableLoaders.keySet.contains(modelType.toLowerCase))
    availableLoaders(modelType).load(sc, path)
  }

//  implicit object LogisticRegressionModelLoader extends ModelLoader[LogisticRegressionModel] {
//    def load(path: String)(implicit sc: SparkContext): LogisticRegressionModel =
//      LogisticRegressionModel.load(sc, path)
//  }

//  def apply[T](modelType: String, path: String)(implicit sc: SparkContext): Model[T] = {
//    require(availableLoaders.keySet.contains(modelType.toLowerCase))
//    Model(availableLoaders(modelType).load(sc, path))
//  }
}
