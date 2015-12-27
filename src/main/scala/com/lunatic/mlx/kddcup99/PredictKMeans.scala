package com.lunatic.mlx.kddcup99

import org.apache.spark.SparkContext
import org.apache.spark.mllib.clustering.KMeansModel
import org.apache.spark.mllib.linalg.{Vectors, Vector}
import org.apache.spark.rdd.RDD

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

    // The following parameters are only used to find the corresponding model
    val maxIterations = 100
    val runs = 3
    val epsilon = 1E-15

    //    val ks = List(150)
    //    val norms = List(ImportData.L2NormV1)

    val ks = (2 to 20).map(k => k * 10)
    val norms = List(ImportData.L0Norm, ImportData.L1Norm, ImportData.L2NormV1, ImportData.L2NormV2)

    for (
      norm <- norms;
      labeledData = sc.objectFile[(String, Vector)](appConf.normDataPath + norm).cache;
      k <- ks;
      params = KMeansParams(k, epsilon, maxIterations, runs, norm);
      modelFile = stubFilePath(params) + ".model";
      model = KMeansModel.load(sc, modelFile)
    ) yield {
      TrainKMeans.evaluate(params, labeledData, labelsCount, model, "_X.md")
      //      predict(model, labeledData.map(_._2))
    }

    //    for(norm <- norms; k <- ks) {
    //      println(s"Loading data: ${appConf.normDataPath + norm}")
    //      val labeledData = sc.objectFile[(String, Vector)](appConf.normDataPath + norm).cache
    //      println(s"Loaded data: ${appConf.normDataPath + norm}")
    //      val params = KMeansParams(k, epsilon, maxIterations, runs, norm)
    //      val modelFile = stubFilePath(params) + ".model"
    //      val model = KMeansModel.load(sc, modelFile)
    //       println(s"Loaded model: $modelFile")
    //      //      predict(model, labeledData.map(_._2))
    //      TrainKMeans.evaluate(params, labeledData, labelsCount, model, "_X.md")
    //    }

  }


  def predict(model: KMeansModel, data: RDD[Vector])
             (implicit sc: SparkContext, appConf: Configuration): Boolean = {

    val predictions = data.map(lp => (model.predict(lp), lp))

    val clusters = model.clusterCenters.zipWithIndex.map(_.swap).toMap

    val averageDistToCentroid = predictions.map {
      case (cluster, vector) =>
        Vectors.sqdist(vector, clusters(cluster))
    }.mean



    println("------------------")
    println("Some predictions and distances")
    predictions.take(10).foreach { p =>
      println(s"* $p")
      val clustDist = clusters.map(c => (c._1, Vectors.sqdist(p._2, c._2)))
      clustDist.toSeq.sortBy(_._2).take(3).foreach(d => println(f"  - ${d._2}%9.6f"))
    }

    //    // Save a small sample to try a graphical representation in R
    //    val sample = data.sample(false, 0.005).map(datum =>
    //      model.predict(datum) + "," + datum.toArray.mkString(",")
    //    ).collect
    //    val centers = clusters.map(cc => cc._1 + "," + cc._2.toArray.mkString(","))
    //    saveLinesToFile(centers ++ sample, appConf.outputPath + "/sample.dat")

    println("------------------")
    println(s"Average Distance to Centroid : $averageDistToCentroid")

    true
  }

}
