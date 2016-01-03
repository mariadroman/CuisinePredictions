package com.lunatic.mlx.kddcup99.transformers

import org.apache.spark.rdd.RDD

/**
  * Transform a A into an B or an RDD[A] into an RDD[B]
  */
trait Transformer[A, B] {

  def transform(input: RDD[A]): RDD[B]

  def transform(input: A): B

}


