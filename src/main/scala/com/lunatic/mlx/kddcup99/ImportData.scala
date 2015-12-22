package com.lunatic.mlx.kddcup99

import org.apache.spark.SparkContext
import org.apache.spark.mllib.feature.Normalizer
import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.apache.spark.rdd.RDD


/**
 * Cleanup data, normalization and persistence
 */
object ImportData extends SparkRunnable {

  // No normalization; return original data
  val NoNorm = "NoNorm"
  // L2Norm applied on each row
  val L2NormRow = "L2NormRow"
  // L1Norm applied by column, using MultivariateStatisticalSummary
  val L1NormColV1 = "L1NormColV1"
  // L2Norm applied by column, using MultivariateStatisticalSummary
  val L2NormColV1 = "L2NormColV1"
  // L2Norm own algorithm applied by column
  val L2NormColV2 = "L2NormColV2"

  val NORMALIZER = L2NormColV2

  def main(args: Array[String]) = {

    DefaultSparkRunner(this.getClass.getName, args).run(this)

  }

  def run(implicit sc: SparkContext, appConf: Configuration) = {

    val (labeledData, labelsCount) = importNormalize

    com.lunatic.mlx.removeHdfsFile(appConf.normDataPath + NORMALIZER)
    labeledData.saveAsObjectFile(appConf.normDataPath + NORMALIZER)

    com.lunatic.mlx.removeHdfsFile(appConf.labelsCountPath)
    sc.parallelize(labelsCount.toSeq).saveAsObjectFile(appConf.labelsCountPath)

  }

  def importNormalize(implicit sc: SparkContext, appConf: Configuration) : (RDD[(String, Vector)], Map[String, Long]) = {

    val lines = sc.textFile(appConf.inputTrainingData).map(_.split(","))

    val labelsCount: Map[String, Long] = lines.map(l => l.last).countByValue().toMap

    // The following are the index of the columns containing discrete data, including the label column
    val symboliColumns = List(1, 2, 3, 6, 11, 20, 21, 41)

    val maps = symboliColumns.map { col =>
      (col, lines.map(arr => arr(col)).distinct.collect.zipWithIndex.toMap)
    }.toMap

    val labelsIndex = maps(41)
    val labelsDict = labelsIndex.map(_.swap)

    val data = lines.map { arr =>
      val values = (0 until arr.size).map { i =>
        val currentValue = arr(i)
        if (symboliColumns.contains(i))
          maps(i)(currentValue).toDouble
        else
          currentValue.toDouble
      }.toArray
      values
    }.cache

    val labels = data.map { row =>
      val label = row.takeRight(1)(0).toInt
      labelsDict(label)
    }
    val rawData: RDD[Array[Double]] = data.map { row =>
      row.dropRight(1)
    }.cache



    val ignorableColumns = columnsWithEqualValues(rawData).toArray

    println(s"Found ${ignorableColumns.filter(i => i).size} ignorable columns.")

    val rawFilteredData =
      if(ignorableColumns.size > 0) {
        rawData.map{v => v.zip(ignorableColumns).filterNot(_._2).map(_._1)}
      }
      else
        rawData

    val rawVectors = rawFilteredData.map(v => Vectors.dense(v))

    val vectors = normalize(rawVectors, L2NormColV1)

    val labeledData: RDD[(String, Vector)] = labels.zip(vectors)

    (labeledData, labelsCount)
  }

  def columnsWithEqualValues(data: RDD[Array[Double]]) = {
    (0 until data.first.size).map(col => data.map(v => v(col)).distinct.count == 1)
  }

  def normalize(data: RDD[Vector], algo: String): RDD[Vector] = algo match {
    case NoNorm => data
    case L2NormRow => new Normalizer().transform(data)
    case L1NormColV1 => normalizeColumnsL2(data)
    case L2NormColV1 => normalizeColumnsL2(data)
    case L2NormColV2 => rawNormalizeColumnsL2(data)
  }

  /**
   * L2 Normalization by column
   * @param data
   * @return
   */
  def normalizeColumnsL2(data: RDD[Vector]) = {
    import org.apache.spark.mllib.stat.{MultivariateStatisticalSummary, Statistics}

    import math._

    val summary: MultivariateStatisticalSummary = Statistics.colStats(data)
    val means = summary.mean.toArray
    val variance = summary.variance.toArray

    data.map{ v =>
      Vectors.dense(
        v.toArray
        .zip(means).map{case(x, m) => m - x}
        .zip(variance).map{ case (x, v) => x / sqrt(v)}
      )
    }

  }
  /**
   * L1 Normalization by column
   * @param data
   * @return
   */
  def normalizeColumnsL1(data: RDD[Vector]) = {
    import org.apache.spark.mllib.stat.{MultivariateStatisticalSummary, Statistics}

    import math._

    val summary: MultivariateStatisticalSummary = Statistics.colStats(data)
    val ranges = summary.max.toArray.zip(summary.min.toArray).map { case (xM, xm) => abs(xM - xm) }
    val averages = summary.max.toArray.zip(summary.min.toArray).map { case (xM, xm) => (xM + xm) / 2 }

    data.map{ v =>
      Vectors.dense(
        v.toArray
          .zip(averages).map{case(x, a) => x - a}
          .zip(ranges).map{ case (x, r) => x / r}
      )
    }
  }

  /**
   * Expanded column L2 Normalization unoptimized
   * @param data
   * @return
   */
  def rawNormalizeColumnsL2(data: RDD[Vector]) = {
    import math._
    val cols = data.first.size
    val size = data.count

    val da = data.map(_.toArray)
    val da2 = data.map(_.toArray).map(arr => arr.map(x => x * x))

    val sums = da.reduce{(v1, v2) =>
      val zv = v1.zip(v2)
      zv.map{case(a, b) => a + b}
    }
    val sum2 = da2.reduce{(v1, v2) =>
      val zv = v1.zip(v2)
      zv.map{case(a, b) => a + b}
    }
    val means = sums.map(_ / size)

    val variance = da.map(v => v.zip(means).map{case(x, m) => (x - m)*(x - m)}).reduce{(v1, v2) =>
      val zv = v1.zip(v2)
      zv.map{case(a, b) => a + b}
    }.map(_ / size)

    val stdev = variance.map(sqrt(_))

    da.map(v => v.zip(means).map{case(x, m) => x - m}.zip(stdev).map{case(x, s) => x / s}).map(Vectors.dense(_))

  }

}
