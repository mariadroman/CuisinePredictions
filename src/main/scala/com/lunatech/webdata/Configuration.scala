package com.lunatech.webdata


object Configuration {

  private val modelRootPath = "working_model"

  val dataPath = s"$modelRootPath/input_data"
  val cuisinesPath = s"$modelRootPath/cuisines"
  val ingredientsPath = s"$modelRootPath/ingredients"
  val recipesPath = s"$modelRootPath/recipes"

  private val trainingDataRoot = s"$modelRootPath/training"

  val naiveBayesPath = trainingDataRoot + "_naiveBayes"

  val logisticRegPath = trainingDataRoot + "_logisticReg"

  val dtGiniPath = trainingDataRoot + "_dt_gini"
  val dtEntropyPath = trainingDataRoot + "_dt_entropy"
  val dtVariancePath = trainingDataRoot + "_dt_variance"

  val rfGiniPath = trainingDataRoot + "_rf_gini"
  val rfEntropyPath = trainingDataRoot + "_rf_entropy"
  val rfVariancePath = trainingDataRoot + "_rf_variance"

}
