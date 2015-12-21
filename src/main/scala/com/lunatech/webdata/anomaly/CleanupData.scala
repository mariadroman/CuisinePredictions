package com.lunatech.webdata.anomaly

import java.io.{BufferedOutputStream, PrintWriter}

import com.lunatech.webdata._
import com.lunatech.webdata.cuisine.Configuration
import org.apache.hadoop.fs.{Path, FileSystem}
import org.apache.spark.mllib.clustering.KMeans
import org.apache.spark.mllib.feature.Normalizer
import org.apache.spark.mllib.linalg.{Vectors, Vector}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}

import scala.util.Try

/**
 *
 */
object CleanupData {

  def main(args: Array[String]) = {

    implicit val configuration = Configuration(args)

    val defConf = new SparkConf(true)
    val conf = defConf.
      setAppName("TrainKMeans").
      setMaster(defConf.get("spark.master",  "local[*]")).
      set("spark.driver.memory", "3g").
      set("spark.executor.memory", "4g").
      set("es.index.auto.create", configuration.es_index_auto_create).
      set("es.nodes", configuration.es_nodes).
      set("es.port", configuration.es_port)

    implicit val sc = new SparkContext(conf)

    val sqc = new SQLContext(sc)

    val inputFile = "/Users/olivertupran/lunatech/data/KDD-Cup-1999/kddcup.data"
//    val inputFile = "/Users/olivertupran/lunatech/data/KDD-Cup-1999/kddcup.data_10_percent"
//    val inputFile = "/Users/olivertupran/lunatech/data/KDD-Cup-1999/kddcup.newtestdata_10_percent_unlabeled"

    val lines = sc.textFile(inputFile).map(_.split(","))

    val colnames = sc.textFile("/Users/olivertupran/lunatech/data/KDD-Cup-1999/kddcup.colnames.txt")
      .map(_.split(":")).map(arr => (arr(0).trim, arr(1).trim == "continuous.")).collect

    val linesCount = lines.count

    val labelsCount = lines.map(l => l.last).countByValue()

    val symboliColumns = List(1, 2, 3, 6, 11, 20, 21, 41)

    val maps = symboliColumns.map{ col =>
      (col, lines.map(arr => arr(col)).distinct.collect.zipWithIndex.toMap)
    }.toMap

    val labelsIndex = maps(41)
    val labelsDict = labelsIndex.map(e => (e._2, e._1))

    val data = lines.map{ arr =>
      val values = (0 until arr.size).map{ i =>
        val currentValue = arr(i)
        if(symboliColumns.contains(i))
          maps(i)(currentValue).toDouble
        else
          currentValue.toDouble
      }.toArray
      values
    }.cache

    val labels = data.map{ row =>
      val label = row.takeRight(1)(0).toInt
      labelsDict(label)
    }
    val rawVectors = data.map{ row =>
      val values = row.dropRight(1)
      Vectors.dense(values.toArray)
    }.cache

    val normalizer = new Normalizer()
    val vectors = normalizer.transform(rawVectors).cache
//    val vectors = normalize(rawVectors)

    val labeledData = labels.zip(vectors)

    labeledData.take(10).foreach(println)

//    val labeledData = data.map{ row =>
//      val label = row.takeRight(1)(0).toInt
//      val values = row.dropRight(1)
//      (labelsDict(label), Vectors.dense(values.toArray))
//    }.cache
//
//    labeledData.take(10).foreach(println)
//
//    val vectors = labeledData.map(_._2)


    // Cluster the data into two classes using KMeans
    val numClusters = 12
    //val numClusters = (labelsIndex.size) * 9
    val numIterations = 2
    val runs = 1
    val epsilon = 1E-15
    val trainer = new KMeans().setK(numClusters)
      .setMaxIterations(numIterations)
      .setRuns(runs)
      .setInitializationMode(KMeans.K_MEANS_PARALLEL)
      .setEpsilon(epsilon)
    val (clusters, trainingRuntime) = timeCode(trainer.run(vectors))

    val stubFile = f"/tmp/kmeans_$epsilon%.0E_$numClusters%03d_$numIterations%04d_$runs%02d_norm1".replaceAll("\\+", "")
    val modelFile = stubFile + ".model"
    removeHdfsFile(modelFile)
    clusters.save(sc, modelFile)

    // Evaluate clustering by computing Within Set Sum of Squared Errors
    val WSSSE = clusters.computeCost(vectors)

    val (predictions, predictionRuntime) =  timeCode(labeledData.map(lp => (lp._1, clusters.predict(lp._2))))

    val predictEval = predictions.groupBy(_._1).collect.map{case (label, pred) =>

      val clustersWeights = pred.map(x => x._2)
        .groupBy(x => x).map(x => (x._1, x._2.size))
        .toSeq.sortBy(x => x._2).reverse
      (label, clustersWeights)
    }.toMap

    val inputParamsStr =
      f"| Input Parameters     |   Value   |" ::
      f"| :------------------- | --------: |" ::
      f"| Training Data Size   | $linesCount%9d |" ::
      f"| Clusters             | $numClusters%9d |" ::
      f"| Iterations           | $numIterations%9d |" ::
      f"| Runs                 | $runs%9d |" ::
      f"| Epsilon              | $epsilon%.3E |" ::
      Nil

    val tabCountHeaderStr = "" ::
      "| Label                |   Total   | Clust | Count   | Clust | Count   | Clust | Count   | Clust | Count   | Clust | Count   | Clust | Count   | Clust | Count   | Clust | Count   | Clust | Count   | Clust | Count   |" ::
      "| -------------------- | --------: | ----: |-------: | ----: |-------: | ----: |-------: | ----: |-------: | ----: |-------: | ----: |-------: | ----: |-------: | ----: |-------: | ----: |-------: | ----: |-------: |" ::
      Nil

    val tabCountStr =
      labelsCount.toSeq.sortBy(_._2).reverse.map{ case(label, count) =>
        val cwp = predictEval(label).take(10)
          .map(x => f"${x._1}%5d | ${x._2}%7d")
          .mkString(" | ")
        f"| ${label}%-20s | ${count}%9d | ${cwp} |"
    }

    val tabPercentageHeaderStr = "" ::
      "| Label                |   Total   | Clust | %(tot)  | Clust | %(tot)  | Clust | %(tot)  | Clust | %(tot)  | Clust | %(tot)  | Clust | %(tot)  | Clust | %(tot)  | Clust | %(tot)  | Clust | %(tot)  | Clust | %(tot)  |" ::
      "| -------------------- | --------: | ----: |-------: | ----: |-------: | ----: |-------: | ----: |-------: | ----: |-------: | ----: |-------: | ----: |-------: | ----: |-------: | ----: |-------: | ----: |-------: |" ::
      Nil

    val tabPercentagesStr =
      labelsCount.toSeq.sortBy(_._2).reverse.map{ case(label, count) =>
        val cwp = predictEval(label).take(10)
          .map{ x =>
            val ratio = x._2.toDouble / count * 100
            f"${x._1}%5d | $ratio%7.3f"}
          .mkString(" | ")
        f"| ${label}%-20s | ${count}%9d | ${cwp} |"
    }

    val resultsStr = "" ::
      f"| Results Info         | Value            |" ::
      f"| :------------------- | ---------------: |" ::
      f"| WSSSE                | $WSSSE%.7E |" ::
      f"| Training Runtime     | ${trainingRuntime/1000/60}%02d:${trainingRuntime/1000%60}%02d (mm:ss) |" ::
      f"| Prediction Runtime   | ${predictionRuntime/1000/60}%02d:${predictionRuntime/1000%60}%02d (mm:ss) |" ::
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
      resultsStr ++
      legendStr

    saveRezToFile(summaryStr, stubFile + ".results")
    println(summaryStr.mkString("\n"))
  }


  def normalize(data: RDD[Vector]) = {
    import org.apache.spark.mllib.stat.{MultivariateStatisticalSummary, Statistics}
    import math._
    val summary: MultivariateStatisticalSummary = Statistics.colStats(data)
    val ranges = summary.max.toArray.zip(summary.min.toArray).map{ case (xM, xm) => (xM - xm)}
    val averages = summary.max.toArray.zip(summary.min.toArray).map{ case (xM, xm) => (xM + xm) / 2}

    data.map{ v =>
      val nv = (0 until v.size).map{ i =>
        // (v(i) - averages(i)) / ranges(i)
        (v(i) - summary.mean(i)) / sqrt(summary.variance(i))
      }
      Vectors.dense(nv.toArray)
    }
  }

  def saveRezToFile(rez: List[String], path: String) = {
    // Create the HDFS file system handle
    val hdfs = FileSystem.get(new org.apache.hadoop.conf.Configuration())
    val writer = new PrintWriter(new BufferedOutputStream(hdfs.create(new Path(path), true)), true)
    rez.foreach(line => Try(writer.print(line + "\n")))
    // Close the streams
    Try(writer.close())

  }


}
