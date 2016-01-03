package com.lunatic.mlx.kddcup99.transformers

import org.apache.spark.rdd.RDD

/**
  * Unlike a normal transformer this one needs to learn something about the data to be transformed
  */
trait EducatedTransformer[T, R] extends Transformer[T, R] {
  def learn(input: RDD[T]): EducatedTransformer[T, R]
}
