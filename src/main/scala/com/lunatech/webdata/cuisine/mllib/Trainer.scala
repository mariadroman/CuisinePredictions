package com.lunatech.webdata.cuisine.mllib

import org.apache.spark.SparkContext
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import com.lunatech.webdata.commons._
/**
 *
 */
trait Trainer {
  def train[T](data: RDD[LabeledPoint], config: Configuration[T])(implicit sc: SparkContext): Model[T]
}
