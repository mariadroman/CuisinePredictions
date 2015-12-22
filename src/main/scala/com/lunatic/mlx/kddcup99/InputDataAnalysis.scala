package com.lunatic.mlx.kddcup99

import org.apache.spark.SparkContext

import scala.io.Source


/**
 * Have a look at the data and give some stats
 */
object InputDataAnalysis extends SparkRunnable {

  def main(args: Array[String]) = {

    DefaultSparkRunner(this.getClass.getName, args).run(this)

  }

  def run(implicit sc: SparkContext, appConf: Configuration) = {

    val analisysResult = analyze

    saveLinesToFile(analisysResult, appConf.outputPath + "/analysis.txt")
    println(analisysResult.mkString("\n"))

  }

  def analyze(implicit sc: SparkContext, appConf: Configuration): Seq[String] = {

    val lines = sc.textFile(appConf.inputTrainingData).map(_.split(","))

    val colTypes = Source.fromInputStream(InputDataAnalysis.getClass.getResourceAsStream("/kddcup99/kddcup.coltypes.txt"))
      .getLines
      .map(_.split(":")).map(arr => (arr(0).trim, arr(1).trim == "continuous."))

    colTypes.foreach(x => println(s"${x._1}  ${x._2}"))
    println(colTypes.size)

    val colsDict = colTypes.map(_._1).zipWithIndex.map(_.swap).toMap

    val distinctCountByColumn = (0 until lines.first.size).
      map(col => lines.map(v => v(col)).countByValue).
      zipWithIndex.map(_.swap)

    val records = lines.count

    val printableResults = distinctCountByColumn.flatMap { col =>
      val colIdx = col._1
      val colHeader = f"Column: ${colIdx}%2d  ${colsDict(colIdx)}" :: Nil
      val counts = col._2.toSeq.sortBy(_._2).reverse
      val heaviestRatio = counts.head._2.toDouble / records
      val heavyValuesStr = if ((1.0 - heaviestRatio) < 1E-4) {
        val highestRatioVal = counts.head
        val lowRatioVals = counts.tail

        f"| ${highestRatioVal._1}%-20s | ${highestRatioVal._2}%7d | ${highestRatioVal._2.toDouble / records * 100}%10.6f" +:
          lowRatioVals.flatMap { lrv =>
            val labels = lines.filter(row => row(colIdx) == lrv._1).groupBy(row => row(41)).collect

            f"| ${lrv._1}%-20s | ${lrv._2}%7d | ${lrv._2.toDouble / records * 100}%10.6f" +:
              labels.map(lc => f"| ${" "}%20s | ${lc._2.size}%7d | ${lc._1}")
          }
      } else Nil

      val eps = 1E-6
      val ignorable =
        if ((1.0 - heaviestRatio) < eps) "* This column might be ignored !!!" :: Nil
        else Nil
      colHeader ++ heavyValuesStr ++ ignorable
    }
    printableResults
  }

}
