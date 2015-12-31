package com.lunatic.mlx.kddcup99.transformers

import org.apache.spark.rdd.RDD

/**
  * Transform a T into an R or an RDD[T] into an RDD[R]
  */
trait Transformer[T, R] {

  def transform(input: RDD[T]): RDD[R]

  def transform(input: T): R

}


