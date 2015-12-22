package com.lunatic.mlx.kddcup99

import com.lunatic.mlx.timeCode
import org.apache.spark.SparkContext
import org.apache.spark.mllib.clustering.KMeansModel
import org.apache.spark.mllib.linalg.{Vectors, Vector}

/**
 * For now this is not a true predictor, but rather a re-evaluator for the existing model; useful
 * for adding more meaningful information about the model in the output (.results_1)
 */
object PredictKMeans extends SparkRunnable {

  def main(args: Array[String]) = {

    //TODO Further parametrize the model generation
    DefaultSparkRunner(this.getClass.getName, args).run(this)

  }

  def run(implicit sc: SparkContext, appConf: Configuration) = {

    val labelsCount = sc.objectFile[(String, Long)](appConf.labelsCountPath).collect.toMap
    val sortedLabelsCount = labelsCount.toSeq.sortBy(_._2).reverse

    val labeledData = sc.objectFile[(String, Vector)](appConf.normDataPath).cache
    val vectors = labeledData.map(_._2).cache
    val dataSize = vectors.count


    val maxIterations = 10
    val runs = 1
    val epsilon = 1E-4
    val norm = ImportData.NORMALIZER

    //    val modelK = (2 to 10).map(k => k * labelsCount.size)
    val modelK = List(1).map(x => 9)
    //    val modelK = (1 to 10).map(k => k * labelsCount.size)

    modelK.foreach(k => predict(k, epsilon, maxIterations, runs, norm))

    def predict(k: Int, epsilon: Double, maxIterations: Int, runs: Int, norm: String) = {

      // Store each model just in case we want to try them later
      val stubPath = stubFilePath(k, epsilon, maxIterations, runs, norm)

      val modelFile = stubPath + ".model"

      println(s"Loaded model from $modelFile")
      val model = KMeansModel.load(sc, modelFile)

      // Evaluate clustering by computing Within Set Sum of Squared Errors
      val WSSSE = model.computeCost(vectors)

      val (predictions, predictionRuntime) = timeCode(labeledData.map(lp => (lp._1, model.predict(lp._2), lp._2)))

      val clusters = model.clusterCenters.zipWithIndex.map(_.swap)

      println("------------------")
      println("Some predictions")
      predictions.take(10).foreach { p =>

        println(s"* $p")
        val clustDist = clusters.map(c => (c._1, Vectors.sqdist(p._3, c._2)))
        clustDist.sortBy(_._2).reverse.take(3).foreach(d => println(f"  - "))

      }

      println("------------------")
      println("Cluster centers")
      model.clusterCenters.foreach(println)

      sys.exit

      val predictEvalByLabel = predictions.groupBy(_._1).collect.map { case (label, pred) =>
        val clustersWeights = pred.map(x => x._2)
          .groupBy(x => x).map(x => (x._1, x._2.size))
          .toSeq.sortBy(x => x._2).reverse
        (label, clustersWeights)
      }.toMap


      val predictEvalByCluster = predictions.groupBy(_._2).collect.map { case (pred, label) =>
        val countByLabel = label.map(x => x._1)
          .groupBy(x => x).map(x => (x._1, x._2.size))
        (pred, countByLabel)
      }.toMap


      val inputParamsStr =
        f"| Input Parameters     |   Value   |" ::
          f"| :------------------- | --------: |" ::
          f"| Training Data Size   | $dataSize%9d |" ::
          f"| Clusters             | $k%9d |" ::
          f"| Iterations           | $maxIterations%9d |" ::
          f"| Runs                 | $runs%9d |" ::
          f"| Epsilon              | $epsilon%.3E |" ::
          Nil

      val tabCountHeaderStr = "" ::
        "| Label                |   Total   | Clust | Count   | Clust | Count   | Clust | Count   | Clust | Count   | Clust | Count   | Clust | Count   | Clust | Count   | Clust | Count   | Clust | Count   | Clust | Count   |" ::
        "| -------------------- | --------: | ----: | ------: | ----: | ------: | ----: | ------: | ----: | ------: | ----: | ------: | ----: | ------: | ----: | ------: | ----: | ------: | ----: | ------: | ----: | ------: |" ::
        Nil

      val tabCountStr =
        sortedLabelsCount.map { case (label, count) =>
          val cwp = predictEvalByLabel(label).take(10)
            .map(x => f"${x._1}%5d | ${x._2}%7d")
            .mkString(" | ")
          f"| ${label}%-20s | ${count}%9d | ${cwp} |"
        }

      val tabPercentageHeaderStr = "" ::
        "| Label                |   Total   | Clust | %(tot)  | Clust | %(tot)  | Clust | %(tot)  | Clust | %(tot)  | Clust | %(tot)  | Clust | %(tot)  | Clust | %(tot)  | Clust | %(tot)  | Clust | %(tot)  | Clust | %(tot)  |" ::
        "| -------------------- | --------: | ----: | ------: | ----: | ------: | ----: | ------: | ----: | ------: | ----: | ------: | ----: | ------: | ----: | ------: | ----: | ------: | ----: | ------: | ----: | ------: |" ::
        Nil

      val tabPercentagesStr =
        sortedLabelsCount.map { case (label, count) =>
          val cwp = predictEvalByLabel(label).take(10)
            .map { x =>
              val ratio = x._2.toDouble / count * 100
              f"${x._1}%5d | $ratio%7.3f"
            }
            .mkString(" | ")
          f"| ${label}%-20s | ${count}%9d | ${cwp} |"
        }

      val tabCountByClustHeader = "" ::
        "| Clust | " + sortedLabelsCount.map { case (label, count) =>
          if (label.length > 7)
            label.substring(0, 6) + "."
          else label
        }.map(s => f"${s}%-7s").mkString(" | ") + " |" ::
        Nil

      val tabCountByClustHeaderSeparator =
        "| ----: | " + sortedLabelsCount.map(x => "------:").mkString(" | ") + " |" ::
          Nil

      val tabCountByClustFooter =
        "| Total | " + sortedLabelsCount.map(s => f"${s._2}%7d").mkString(" | ") + " |" ::
          Nil

      val tabCountByClust = predictEvalByCluster.toSeq.sortBy(_._1).map { case (clust, count) =>
        val countByLabel = sortedLabelsCount.
          map(lc => count.getOrElse(lc._1, 0)).
          map(x => f"$x%7d".replaceAll("0", " ")).
          mkString(" | ")
        f"| $clust%5d | $countByLabel |"
      }

      val tabPercentageByClust = predictEvalByCluster.toSeq.sortBy(_._1).map { case (clust, count) =>
        val countByLabel = sortedLabelsCount.
          map { case (label, total) => count.getOrElse(label, 0).toDouble / total * 100 }.
          map(x => f"$x%7.3f".replaceAll("0.000", "     ")).
          mkString(" | ")
        f"| $clust%5d | $countByLabel |"
      }

      val resultsStr = "" ::
        f"| Results Info         | Value         |" ::
        f"| :------------------- | ------------: |" ::
        f"| WSSSE                | $WSSSE%.7E |" ::
        //        f"| Training Runtime     | ${trainingRuntime / 1000 / 60}%02d:${trainingRuntime / 1000 % 60}%02d (mm:ss) |" ::
        f"| Prediction Runtime   | ${predictionRuntime / 1000 / 60}%02d:${predictionRuntime / 1000 % 60}%02d (mm:ss) |" ::
        Nil

      val legendStr = "" ::
        ("| Legend ||") ::
        ("| ------ | -------------------------------- |") ::
        ("| WSSSE  | Within Set Sum of Squared Errors |") ::
        ("| Clust  | Cluster Id                       |") ::
        Nil

      val summaryStr = inputParamsStr ++
        tabCountHeaderStr ++
        tabCountStr ++
        tabPercentageHeaderStr ++
        tabPercentagesStr ++
        tabCountByClustHeader ++
        tabCountByClustHeaderSeparator ++
        tabCountByClust ++
        tabCountByClustFooter ++
        tabCountByClustHeader ++
        tabCountByClustHeaderSeparator ++
        tabPercentageByClust ++
        tabCountByClustFooter ++
        resultsStr ++
        legendStr

      saveLinesToFile(summaryStr, stubPath + ".results_1")
      println(summaryStr.mkString("\n"))
    }
  }

}
