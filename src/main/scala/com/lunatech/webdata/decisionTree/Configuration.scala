package com.lunatech.webdata.decisionTree

/**
 *
 */
class Configuration(val trainingDataPath: String = "myModel/dt/model")
  extends com.lunatech.webdata.Configuration

object Configuration {
  def apply() = new Configuration()
}
