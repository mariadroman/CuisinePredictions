package com.lunatech.webdata.decisionTree

/**
 *
 */
class Configuration(val trainingDataPath: String = "myModel/dt/model")
  extends com.lunatech.webdata.Configuration {

  val giniPath = trainingDataPath + "_gini"
  val entropyPath = trainingDataPath + "_entropy"
  val variancePath = trainingDataPath + "_variance"

}

object Configuration {
  def apply() = new Configuration()
}
