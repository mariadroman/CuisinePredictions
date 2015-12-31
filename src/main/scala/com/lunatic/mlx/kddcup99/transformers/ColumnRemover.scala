package com.lunatic.mlx.kddcup99.transformers

import org.apache.spark.rdd.RDD

import scala.reflect.ClassTag

/**
  * Remove the columns in the given list
  */
case class ColumnRemover[T: ClassTag](removableColumns: List[Int]) extends Transformer[Array[T], Array[T]] {

  override def transform(input: RDD[Array[T]]): RDD[Array[T]] =
    if (removableColumns.size > 0)
      // to save some CPU time we don't map the transform
      input.map { v => v.zipWithIndex.filterNot(x => removableColumns.contains(x._2)).map(_._1) }
    else
      input

  override def transform(input: Array[T]): Array[T] = {
    if (removableColumns.size > 0)
      input.zipWithIndex.filterNot(x => removableColumns.contains(x._2)).map(_._1)
    else
      input
  }
}
