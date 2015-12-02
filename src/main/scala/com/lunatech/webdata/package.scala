package com.lunatech

import com.lunatech.webdata.cuisine.mllib.Model
import org.apache.commons.io.FileUtils
import org.apache.spark.mllib.evaluation.MulticlassMetrics

/**
  * Common utilities
  */
package object webdata {

  def removeDir(path: String) = {
    val dir = new java.io.File(path)
    if (dir exists)
      FileUtils.deleteDirectory(dir)
  }


  def printEvaluationMetrics[T](model: Model[T], metrics: MulticlassMetrics) = {

    println(s"\n### ${model.self.getClass.getSimpleName} model evaluation")
    println( "| Parameter                    | Value     |")
    println( "| :--------------------------- | --------: |")
    println(f"| Precision                    | ${metrics.precision * 100}%8.4f%% |")
    println(f"| Error                        | ${(1 - metrics.precision) * 100}%8.4f%% |")
    println(f"| Weighted Precision           | ${metrics.weightedPrecision * 100}%8.4f%% |")
    println(f"| Weighted True Positive Rate  | ${metrics.weightedTruePositiveRate * 100}%8.4f%% |")
    println(f"| Weighted False Positive Rate | ${metrics.weightedFalsePositiveRate * 100}%8.4f%% |")

  }

  /**
   * Run a block and return the block result and the runtime in millis
   * @param block
   * @return
   */
  def timeCode[T](block : => T): (T, Long) = {
    val start = new java.util.Date
    val result = block
    val runtime = (new java.util.Date).toInstant.toEpochMilli - start.toInstant.toEpochMilli
    (result, runtime)
  }

}
