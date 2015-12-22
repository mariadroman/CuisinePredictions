package com.lunatic.mlx.kddcup99

import com.lunatic.mlx.{removeHdfsFile, timeCode}
import org.apache.spark.SparkContext
import org.apache.spark.mllib.clustering.KMeans
import org.apache.spark.mllib.linalg.Vector

/**
 * Train KMeans model(s)
 */
object TrainKMeans extends SparkRunnable {

  def main(args: Array[String]) = {

    //TODO Further parametrize the model generation
    DefaultSparkRunner(this.getClass.getName, args).run(this)

  }

  def run(implicit sc: SparkContext, appConf: Configuration) = {

    val labelsCount = sc.objectFile[(String, Long)](appConf.labelsCountPath).collect.toMap
    val sortedLabelsCount = labelsCount.toSeq.sortBy(_._2).reverse

    val labeledData = sc.objectFile[(String, Vector)](appConf.normDataPath + ImportData.NORMALIZER).cache
    val vectors = labeledData.map(_._2).cache
    val dataSize = vectors.count

    val maxIterations = 100
    val runs = 3
    val epsilon = 1E-15
    val norm = ImportData.NORMALIZER

//    val trainK = (2 to 10).map(k => k * labelsCount.size)
    val trainK = List(7).map(k => k * labelsCount.size)

    trainK.foreach(k => train(k, epsilon, maxIterations, runs, norm))

    def train(k: Int, epsilon: Double, maxIterations: Int, runs: Int, norm: String) = {

      val trainer = new KMeans().setK(k).
        setMaxIterations(maxIterations).
        setRuns(runs).
        setInitializationMode(KMeans.K_MEANS_PARALLEL).
        setEpsilon(epsilon)

      val (model, trainingRuntime_ms) = timeCode(trainer.run(vectors))

      val stubPath = stubFilePath(k, epsilon, maxIterations, runs, norm)

      val modelFile = stubPath + ".model"

      println(s"Saving model to $modelFile")
      removeHdfsFile(modelFile)
      model.save(sc, modelFile)

      // Evaluate clustering by computing Within Set Sum of Squared Errors
      val WSSSE = model.computeCost(vectors)

      val (predictions, predictionRuntime) = timeCode(labeledData.map(lp => (lp._1, model.predict(lp._2))))

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
        "| Clust | " + sortedLabelsCount.map{ case (label, count) =>
          if(label.length > 7)
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

      val tabCountByClust = predictEvalByCluster.toSeq.sortBy(_._1).map{ case (clust, count) =>
        val countByLabel = sortedLabelsCount.
          map{case (label, total) =>
            val rez = count.getOrElse(label, 0)
            if(rez != 0)
              f"$rez%7d"
            else "       "
          }.mkString(" | ")

        f"| $clust%5d | $countByLabel |"
      }

      val tabPercentageByClust = predictEvalByCluster.toSeq.sortBy(_._1).map{ case (clust, count) =>
        val countByLabel = sortedLabelsCount.
          map{case (label, total) =>
            val rez = count.getOrElse(label, 0).toDouble / total * 100
            if(rez != 0.0)
              f"$rez%7.3f"
            else "       "
          }.mkString(" | ")

        f"| $clust%5d | $countByLabel |"
      }

      val resultsStr = "" ::
        f"| Results Info         | Value         |" ::
        f"| :------------------- | ------------: |" ::
        f"| WSSSE                | $WSSSE%.7E |" ::
        f"| Training Runtime     | ${trainingRuntime_ms / 1000 / 60}%02d:${trainingRuntime_ms / 1000 % 60}%02d (mm:ss) |" ::
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

      saveLinesToFile(summaryStr, stubPath + ".results")
      println(summaryStr.mkString("\n"))
    }
  }

}
