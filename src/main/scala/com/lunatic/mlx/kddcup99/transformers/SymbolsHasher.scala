package com.lunatic.mlx.kddcup99.transformers

import org.apache.spark.rdd.RDD

/**
  * Given a list of symbolic columns (usually non-numeric) map it into a list of doubles and save the map.
  *
  * Ideally this should be an isomorphic transformer (@see IsoTransformer).
  */
case class SymbolsHasher(symboliColumns: List[Int], dictionary: Option[Map[Int, Map[String, Int]]])
  extends EducatedTransformer[Array[String], Array[Double]] {

  def this(symboliColumns: List[Int]) = this(symboliColumns, None)

  override def learn(input: RDD[Array[String]]): EducatedTransformer[Array[String], Array[Double]] = {
    val maps: Map[Int, Map[String, Int]] = symboliColumns.map { col =>
      (col, input.map(arr => arr(col)).distinct.collect.zipWithIndex.toMap)
    }.toMap
    SymbolsHasher(symboliColumns, Some(maps))
  }

  /**
    * Hash the symbols into integers then convert everything to doubles.
    *
    * !! ATTENTION !! This can fail, and so it should until further refactoring.
    * What do we do if in the test data we have undefined labels? Ideally we should educate a new SymbolHasher
    * by adding a new entry then and persist it. Continuous learning, or so they say...
    *
    * @param input
    * @return
    */
  override def transform(input: RDD[Array[String]]): RDD[Array[Double]] = {
    require(dictionary.isDefined)
    input.map(transform)
  }

  /**
    * Hash the symbols into integers then convert everything to doubles.
    *
    * !! ATTENTION !! This can fail, and so it should until further refactoring.
    * What do we do if in the test data we have undefined labels? Ideally we should educate a new SymbolHasher
    * by adding a new entry then and persist it. Continuous learning, or so they say...
    *
    * @param input
    * @return
    */
  override def transform(input: Array[String]): Array[Double] = {
    require(dictionary.isDefined)
    input.zipWithIndex.map { case (currentValue, col) =>
      if (dictionary.get.keys.toSeq.contains(col))
        dictionary.get.get(col).get(currentValue).toDouble
      else
        currentValue.toDouble
    }
  }
}
