package com.lunatech.webdata.anomaly

import com.lunatech.webdata._
import com.lunatech.webdata.cuisine.Configuration
import org.apache.spark.mllib.clustering.KMeans
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}

/**
 *
 */
object ExportToESRaw {

  def main(args: Array[String]) = {

    implicit val configuration = Configuration(args)

    val defConf = new SparkConf(true)
    val conf = defConf.setAppName("CuisineRecipesImportData").
      setMaster(defConf.get("spark.master",  "local[*]")).
      set("spark.driver.memory", "5g").
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

//    colnames.foreach(x => println(s"${x._1}  ${x._2}"))
//    println(colnames.size)
//
//    val schema = StructType {
//      colnames.map {
//        _ match {
//          case (cn, true) => StructField(cn, DoubleType, true)
//          case (sn, false) => StructField(sn, StringType, true)
//        }
//      }
//    }
//    val typedData = lines.map{ arr =>
//      val values = (0 until arr.size).map{ i =>
//        val currentValue = arr(i)
//        if(colnames(i)._2)
//          currentValue.toDouble
//        else
//          currentValue
//      }.toArray
//      values
//    }
//
//    schema.printTreeString()
//
//    val dataDF = sqc.createDataFrame(typedData.map(a => Row.fromSeq(a)), schema)
//
//
//    import org.elasticsearch.spark.sql._
//    dataDF.saveToEs("kddcup/raw")
//
//    sys.exit

    val linesCount = lines.count

    val labelsCount = lines.map(l => l.last).countByValue()

    val symboliColumns = List(1, 2, 3, 6, 11, 20, 21, 41)

    val maps = symboliColumns.map{ col =>
      (col, lines.map(arr => arr(col)).distinct.collect.zipWithIndex.toMap)
    }.toMap

    val labelsIndex = maps(41)
    val labelsDict = labelsIndex.map(e => (e._2, e._1)).toMap

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

    val vectors = normalize(rawVectors)

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
//    val vectors = normalize(labeledData.map(_._2))


    // Cluster the data into two classes using KMeans
    val numClusters = 12
    //val numClusters = (labelsIndex.size) * 9
    val numIterations = 10
    val runs = 3
    val (clusters, trainingRuntime) = timeCode(KMeans.train(vectors, numClusters, numIterations, runs))

    val modelFile = f"/tmp/kmeans_$numClusters%03d_$numIterations%04d_$runs%02d_norm1.model"
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

    println(f"| Input Parameters     |   Value   |")
    println(f"| :------------------- | --------: |")
    println(f"| Training Data Size   | $linesCount%9d |")
    println(f"| Clusters             | $numClusters%9d |")
    println(f"| Iterations           | $numIterations%9d |")
    println(f"| Runs                 | $runs%9d |")

    println("")
    println("| Label                |   Total   | Clust | Count   | Clust | Count   | Clust | Count   | Clust | Count   | Clust | Count   | Clust | %(tot)  | Clust | %(tot)  | Clust | %(tot)  | Clust | %(tot)  | Clust | %(tot)  |")
    println("| -------------------- | --------: | ----: |-------: | ----: |-------: | ----: |-------: | ----: |-------: | ----: |-------: | ----: |-------: | ----: |-------: | ----: |-------: | ----: |-------: | ----: |-------: |")
    labelsCount.toSeq.sortBy(_._2).reverse.foreach{ case(label, count) =>
      val cwp = predictEval(label).take(10)
        .map(x => f"${x._1}%5d | ${x._2}%7d")
        .mkString(" | ")
      println(f"| ${label}%-20s | ${count}%9d | ${cwp} |")

    }


    println("")
    println("| Label                |   Total   | Clust | %(tot)  | Clust | %(tot)  | Clust | %(tot)  | Clust | %(tot)  | Clust | %(tot)  | Clust | %(tot)  | Clust | %(tot)  | Clust | %(tot)  | Clust | %(tot)  | Clust | %(tot)  |")
    println("| -------------------- | --------: | ----: |-------: | ----: |-------: | ----: |-------: | ----: |-------: | ----: |-------: | ----: |-------: | ----: |-------: | ----: |-------: | ----: |-------: | ----: |-------: |")
    labelsCount.toSeq.sortBy(_._2).reverse.foreach{ case(label, count) =>
      val cwp = predictEval(label).take(10)
        .map{ x =>
          val ratio = x._2.toDouble / count * 100
          f"${x._1}%5d | $ratio%7.3f"}
        .mkString(" | ")
      println(f"| ${label}%-20s | ${count}%9d | ${cwp} |")

    }

    println("")
    println(f"| Results Info         | Value            |")
    println(f"| :------------------- | ---------------: |")
    println(f"| WSSSE                | $WSSSE |")
    println(f"| Training Runtime     | ${trainingRuntime/1000/60}%02d:${trainingRuntime/1000%60}%02d (mm:ss) |")
    println(f"| Prediction Runtime   | ${predictionRuntime/1000/60}%02d:${predictionRuntime/1000%60}%02d (mm:ss) |")

    println("")
    val legend = "" ::
      ("| Legend ||") ::
      ("| ------ | -------------------------------- |") ::
      ("| WSSSE  | Within Set Sum of Squared Errors |") ::
      ("| Clust  | Cluster Id                       |") ::
      Nil
    println(legend.mkString("\n"))
  }

  import org.apache.spark.mllib.linalg.Vector

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


}
