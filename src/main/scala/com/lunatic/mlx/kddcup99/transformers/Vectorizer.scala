package com.lunatic.mlx.kddcup99.transformers

import org.apache.spark.mllib.linalg.{Vectors, Vector}
import org.apache.spark.rdd.RDD

/**
  * Basic transformer rendering an array of doubles into a linalg.Vector
  */
case class Vectorizer() extends Transformer[Array[Double], Vector] {

  override def transform(input: RDD[Array[Double]]): RDD[Vector] = input.map(transform)

  override def transform(input: Array[Double]): Vector = Vectors.dense(input)

}
