package com.lunatech.webdata.cuisine

/**
  * Some basic paths configuration
  */
object Configuration {

  // TODO: Refactor and improve

  val inputTrainingData = "data/cuisines/train.json"
  val inputTestingData = "data/cuisines/test.json"

  private val modelRootPath = "working_model"

  val dataPath = s"$modelRootPath/flow_data"
  val recipesPath = s"$modelRootPath/recipes"

  private val trainingDataRoot = s"$modelRootPath/training/"

  val naiveBayesPath = trainingDataRoot + "naive_bayes"

  val logisticRegPath = trainingDataRoot + "logistic_regression"

  val decisionTreePath = trainingDataRoot + "decision_tree"

  val randomForestPath = trainingDataRoot + "random_forest"

  val gbtPath = trainingDataRoot + "gbt"

}
