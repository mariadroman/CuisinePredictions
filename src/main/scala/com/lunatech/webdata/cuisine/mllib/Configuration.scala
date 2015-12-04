package com.lunatech.webdata.cuisine.mllib

import org.apache.spark.ml.classification.LogisticRegression
import org.apache.spark.mllib.tree.{RandomForest, DecisionTree}

/**
 *
 */
trait Nameable {
  def name: String
}

trait Configuration[T] extends Nameable {

}

case class LogisticRegressionConfiguration(
                                     maxDepth: Int = 5,
                                     maxBins: Int = 32,
                                     impurity: String = "gini")
  extends Configuration[LogisticRegression] {
  val name = "LogisticRegression"
}

case class DecisionTreeConfiguration(maxDepth: Int = 5,
                                     maxBins: Int = 32,
                                     impurity: String = "gini")
  extends Configuration[DecisionTree] {
  val name = "DecisionTree"
}

case class RandomForestConfiguration(maxDepth: Int = 5,
                                     maxBins: Int = 32,
                                     impurity: String = "gini",
                                     numTrees: Int = 12,
                                     featureSubsetStrategy: String = "auto")
  extends Configuration[RandomForest] {
  val name = "RandomForest"
}



