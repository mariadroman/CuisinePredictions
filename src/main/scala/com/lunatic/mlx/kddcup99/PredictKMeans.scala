package com.lunatic.mlx.kddcup99

import com.lunatic.mlx.kddcup99.transformers.{InputAnalyzer, DataNormalizer}
import org.apache.spark.SparkContext
import org.apache.spark.mllib.clustering.KMeansModel
import org.apache.spark.mllib.linalg.{Vector, Vectors}
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
    predict
  }

  def evaluate(implicit sc: SparkContext, appConf: Configuration): Unit = {


    val analyser = loadObjectFromFile[InputAnalyzer[String]](appConf.analyzerModelPath).get

    //TODO Ugly!!! Redo!
    val labelsCount = analyser.distinctCountByColumn.get.get(41).get

    // The following parameters are only used to find the corresponding model
    val maxIterations = 50
    val runs = 3
    val epsilon = 1E-10

    //      val ks = (2 to 20).map(k => k * 10)
    //      val norms = List(ImportData.L0Norm, ImportData.L1Norm, ImportData.L2NormV1, ImportData.L2NormV2)
    val ks = List(50)
    val norms = List(DataNormalizer.L2NormV1)

    for (
      norm <- norms;
      trainNormData = sc.objectFile[(String, Vector)](appConf.trainDataPath(norm));
      testNormData = sc.objectFile[(String, Vector)](appConf.testDataPath(norm));
      labels = trainNormData.map(_._1);
      k <- ks;
      params = KMeansParams(k, epsilon, maxIterations, runs, norm);
      modelFile = stubFilePath(params) + ".model";
      model = KMeansModel.load(sc, modelFile)
    ) yield {
      //      println("Sleeping 2 minutes ... (cooling of my CPU)")
      //      Thread.sleep(120000)
            TrainKMeans.evaluate(params, trainNormData, labelsCount, model, "_train_X.md")
            TrainKMeans.evaluate(params, testNormData, labelsCount, model, "_test_X.md")
      //      println("Sleeping 1 minutes ... (cooling of my CPU)")
      //      Thread.sleep(60000)
    }

  }

  def predict(implicit sc: SparkContext, appConf: Configuration): Unit = {


    val analyser = loadObjectFromFile[InputAnalyzer[String]](appConf.analyzerModelPath).get

    //TODO Ugly!!! Redo!
    val labelsCount = analyser.distinctCountByColumn.get.get(41).get


    // The following parameters are only used to find the corresponding model
    val maxIterations = 100
    val runs = 3
    val epsilon = 1E-10

    //      val ks = (2 to 20).map(k => k * 10)
    //      val norms = List(ImportData.L0Norm, ImportData.L1Norm, ImportData.L2NormV1, ImportData.L2NormV2)
    val ks = List(100)
    val norms = List(DataNormalizer.L2NormV1)

    val threshold = 2.507

    val files = Seq((appConf.trainDataPath, "train"), (appConf.testDataPath, "test"))

    for (
      norm <- norms;
      transformer = loadObjectFromFile[KddTransformer](appConf.transformerModelPath(norm)).get;
      k <- ks;
      params = KMeansParams(k, epsilon, maxIterations, runs, norm);
      modelFile = stubFilePath(params) + ".model";
      model = KMeansModel.load(sc, modelFile);
      file <- files
    ) yield {
      //      coolMyCPU(120)
      //      TrainKMeans.evaluate(params, trainNormData, labelsCount, model, "_train_X.md")
      //      TrainKMeans.evaluate(params, testNormData, labelsCount, model, "_test_X.md")

      val labeledData = sc.objectFile[Array[String]](file._1);
      val labels = labeledData.map(_.takeRight(1)(0))
      val unlabeledData = labeledData.map { _.dropRight(1) }

      def predictions = predict(model, transformer, unlabeledData, threshold)
      val results = predictionResults(labels.zip(predictions), threshold)
      val predFile = stubFilePath(params) + s"${file._2}.prediction"
      saveLinesToFile(results, predFile)

    }

  }


  def predict(model: KMeansModel, transformer: KddTransformer, data: RDD[Array[String]], threshold: Double)
             (implicit sc: SparkContext, appConf: Configuration) = {

    val clusters = model.clusterCenters.zipWithIndex.map(_.swap).toMap

    val predictions: RDD[(Boolean, Int, Double, Array[String], Vector)] = data.map { lp =>
      val vec = transformer.transform(lp)
      val clust = model.predict(vec)
      val dist = Vectors.sqdist(vec, clusters(clust))
      val isAnom = dist > threshold
      (isAnom, clust, dist, lp, vec)
    }
    predictions

  }


  def predictionResults(predictions: RDD[(String, (Boolean, Int, Double, Array[String], Vector))], threshold: Double) = {
    val totalPredictions = predictions.count()

    val anomalies = predictions.filter{ case (label, (isAnom, clust, dist, lp, vec)) => isAnom}

    val totalAnno = anomalies.count()

    val resLines = "" +:
      f"Distance Threshold: $threshold%12.5f" +:
      f"Total predictions:  $totalPredictions" +:
      f"Total anomalies:    $totalAnno" +:
      "" +:
      f"| ${"Label"}%-20s | ${"Clust"}%-5s | ${"Distance"}%-12s | ${"threshold"}%-12s | Input Vector | Normalized Vector" +:
      anomalies.map { case (label, (isAnom, clust, dist, lp, vec)) =>
        f"| $label%-20s | $clust%5d | $dist%12.5f | ${lp.mkString(", ")} | ${vec.toArray.mkString(", ")}"
      }.collect.toSeq

    resLines
  }

}
