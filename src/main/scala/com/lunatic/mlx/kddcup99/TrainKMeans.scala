package com.lunatic.mlx.kddcup99

import com.lunatic.mlx.{removeHdfsFile, timeCode}
import org.apache.spark.SparkContext
import org.apache.spark.mllib.clustering.{KMeansModel, KMeans}
import org.apache.spark.mllib.linalg.{Vectors, Vector}
import org.apache.spark.rdd.RDD

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

    val maxIterations = 100
    val runs = 3
    val epsilon = 1E-15

    val ks = (2 to 20).map(k => k * 10)
    val norms = List(ImportData.L0Norm, ImportData.L1Norm, ImportData.L2NormV1, ImportData.L2NormV2)

    for (
      k <- ks;
      norm <- norms;
      labeledData = sc.objectFile[(String, Vector)](appConf.normDataPath + norm).cache;
      k <- ks;
      params = KMeansParams(k, epsilon, maxIterations, runs, norm)
    ) yield {
      val (model, trainingRuntime_ms) = train(params, labelsCount, labeledData)
      val modelFile = stubFilePath(params) + ".model"
      println(s"Saving model to $modelFile")
      removeHdfsFile(modelFile)
      model.save(sc, modelFile)
      println("Sleeping 2 minutes ... (cooling of my CPU)")
      Thread.sleep(120000)
      evaluate(params, labeledData, labelsCount, model, trainingRuntime_ms, ".md")
      println("Sleeping 1 minutes ... (cooling of my CPU)")
      Thread.sleep(60000)
    }
  }


  def train(params: KMeansParams, labelsCount: Map[String, Long])
           (implicit sc: SparkContext, appConf: Configuration): Unit = {

    val labeledData = sc.objectFile[(String, Vector)](appConf.normDataPath + params.norm).cache

    train(params, labelsCount, labeledData)
  }

  def train(params: KMeansParams, labelsCount: Map[String, Long], labeledData: RDD[(String, Vector)])
           (implicit sc: SparkContext, appConf: Configuration): (KMeansModel, Long) = {

    val vectors = labeledData.map(_._2)

    val trainer = new KMeans().
      setK(params.k).
      setMaxIterations(params.maxIterations).
      setRuns(params.runs).
      setInitializationMode(KMeans.K_MEANS_PARALLEL).
      setEpsilon(params.epsilon)

    timeCode(trainer.run(vectors))

  }

  def evaluate(params: KMeansParams, labeledData: RDD[(String, Vector)], labelsCount: Map[String, Long],
               model: KMeansModel, suffix: String)(implicit sc: SparkContext, appConf: Configuration): Unit = {
    evaluate(params, labeledData, labelsCount, model, -1, suffix: String)
  }

  def evaluate(params: KMeansParams, labeledData: RDD[(String, Vector)], labelsCount: Map[String, Long],
               model: KMeansModel, trainingRuntime_ms: Long, suffix: String)
              (implicit sc: SparkContext, appConf: Configuration): Unit = {

    val vectors = labeledData.map(_._2)

    val dataSize = labeledData.count

    val sortedLabelsCount = labelsCount.toSeq.sortBy(_._2).reverse

    // Evaluate clustering by computing Within Set Sum of Squared Errors
    val WSSSE = model.computeCost(vectors)

    val (preds, predictionRuntime) = timeCode(labeledData.map(lp => (lp._1, model.predict(lp._2), lp._2)))

    val predictions = preds.cache()

    val labelsAndClusters = predictions.map(p => (p._1, p._2)).cache

    val clustersAndLabels = labelsAndClusters.map(_.swap).cache

    val labelsInCluster = clustersAndLabels.groupByKey.values
    val labelCounts = labelsInCluster.map(
      _.groupBy(l => l).map(_._2.size))
    val entropyScore = labelCounts.map(m => m.sum * entropy(m)).sum / dataSize

    val clusters = model.clusterCenters.zipWithIndex.map(_.swap).toMap

    val averageCentroidRadius = predictions.map {
      case (label, cluster, vector) =>
        Vectors.sqdist(vector, clusters(cluster))
    }.mean

    val predictEvalByLabel = labelsAndClusters.groupByKey.map { case (label, cluster) =>
      val countByCluster = cluster
        .groupBy(x => x).map(x => (x._1, x._2.size))
        .toSeq.sortBy(x => x._2).reverse
      (label, countByCluster)
    }.collect.toMap


    val predictEvalByCluster = clustersAndLabels.groupByKey.map { case (cluster, label) =>
      val countByLabel = label
        .groupBy(x => x).map(x => (x._1, x._2.size))
      (cluster, countByLabel)
    }.collect.toMap

    val title = f"## Experiment ${experimentId(params)}" :: "" :: Nil

    val inputParamsStr =
      f"| Input Parameters     |   Value   |" ::
        f"| :------------------- | --------: |" ::
        f"| Training Data Size   | $dataSize%9d |" ::
        f"| Clusters             | ${params.k}%9d |" ::
        f"| Iterations           | ${params.maxIterations}%9d |" ::
        f"| Runs                 | ${params.runs}%9d |" ::
        f"| Epsilon              | ${params.epsilon}%.3E |" ::
        f"| Normalization        | ${params.norm}%9s |" ::
        Nil

    val tabCountHeaderStr = "" :: "" ::
      "### Count per label per cluster" :: "" ::
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

    val tabPercentageHeaderStr = "" :: "" ::
      "### Percentage per label per cluster" :: "" ::
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

    val tabCountByClustTitle = "" :: "" ::
      "### Count per cluster per label" :: Nil

    val tabCountByClust = predictEvalByCluster.toSeq.sortBy(_._1).map { case (clust, count) =>
      val countByLabel = sortedLabelsCount.
        map { case (label, total) =>
          val rez = count.getOrElse(label, 0)
          if (rez != 0)
            f"$rez%7d"
          else "       "
        }.mkString(" | ")

      f"| $clust%5d | $countByLabel |"
    }

    val tabPercentageByClustTitle = "" :: "" ::
      "### Percentage per cluster per label" :: Nil

    val tabPercentageByClust = predictEvalByCluster.toSeq.sortBy(_._1).map { case (clust, count) =>
      val countByLabel = sortedLabelsCount.
        map { case (label, total) =>
          val rez = count.getOrElse(label, 0).toDouble / total * 100
          if (rez != 0.0)
            f"$rez%7.3f"
          else "       "
        }.mkString(" | ")

      f"| $clust%5d | $countByLabel |"
    }

    val resultsStr = "" :: "" ::
      "### Results" :: "" ::
      f"| Results Info         | Value         |" ::
      f"| :------------------- | ------------: |" ::
      f"| WSSSE                | $WSSSE%.7E |" ::
      f"| Entropy Score        | $entropyScore%13.11f |" ::
      f"| Average Radius       | $averageCentroidRadius%13.11f |" ::
      f"| Training Runtime     | ${trainingRuntime_ms / 1000 / 60}%02d:${trainingRuntime_ms / 1000 % 60}%02d (mm:ss) |" ::
      f"| Prediction Runtime   | ${predictionRuntime / 1000 / 60}%02d:${predictionRuntime / 1000 % 60}%02d (mm:ss) |" ::
      Nil

    val legendStr = "" ::
      ("| Legend ||") ::
      ("| ------ | -------------------------------- |") ::
      ("| WSSSE  | Within Set Sum of Squared Errors |") ::
      ("| Clust  | Cluster Id                       |") ::
      Nil

    val summaryStr = title ++
      inputParamsStr ++
      resultsStr ++
      legendStr ++
      tabCountHeaderStr ++
      tabCountStr ++
      tabPercentageHeaderStr ++
      tabPercentagesStr ++
      tabCountByClustTitle ++
      tabCountByClustHeader ++
      tabCountByClustHeaderSeparator ++
      tabCountByClust ++
      tabCountByClustFooter ++
      tabPercentageByClustTitle ++
      tabCountByClustHeader ++
      tabCountByClustHeaderSeparator ++
      tabPercentageByClust ++
      tabCountByClustFooter

    saveLinesToFile(summaryStr, stubFilePath(params) + suffix)
    println(summaryStr.mkString("\n"))
  }

  def entropy(counts: Iterable[Int]) = {
    val values = counts.filter(_ > 0)
    val n: Double = values.sum
    values.map { v =>
      val p = v / n
      -p * math.log(p)
    }.sum
  }

  def clusteringScoreEntropy(normalizedLabelsAndData: RDD[(String, Vector)],
                             model: KMeansModel) = {

    val labelsAndClusters =
      normalizedLabelsAndData.mapValues(model.predict)
    val clustersAndLabels = labelsAndClusters.map(_.swap)
    val labelsInCluster = clustersAndLabels.groupByKey().values
    val labelCounts = labelsInCluster.map(
      _.groupBy(l => l).map(_._2.size))
    val n = normalizedLabelsAndData.count()
    labelCounts.map(m => m.sum * entropy(m)).sum / n
  }
}
