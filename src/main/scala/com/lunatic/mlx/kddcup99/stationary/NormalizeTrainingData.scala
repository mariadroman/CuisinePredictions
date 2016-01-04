package com.lunatic.mlx.kddcup99.stationary

import com.lunatic.mlx.kddcup99._
import com.lunatic.mlx.kddcup99.transformers._
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD


/**
  * Prepare training data: split into training and test by given ratio
  * (trying to get the same ratio for each label, just to keep things in balance)
  * and normalize the data by the known normalization algorithms.
  *
  */
object NormalizeTrainingData extends SparkRunnable {

  def main(args: Array[String]) = {

    DefaultSparkRunner(this.getClass.getName, args).run(this)

  }

  def run(implicit sc: SparkContext, appConf: Configuration) = {

    val normalizers = List(DataNormalizer.L2NormV1)

    val files = List(appConf.trainDataPath, appConf.testDataPath)

    val transformers = normalizers.map(normalizer =>
      (normalizer, loadObjectFromFile[KddTransformer](appConf.transformerModelPath(normalizer)))).toMap

    for(
      file <- files;
      normalizer <- normalizers
    ) yield {
      // Load un-normalized training set
      val labeledData = sc.objectFile[Array[String]](file);
      //Normalize and save training set;
      val trainingData = prepareData(labeledData, transformers(normalizer).get)
      val outFile = file + "_" + normalizer
      com.lunatic.mlx.removeHdfsFile(outFile)
      trainingData.saveAsObjectFile(outFile)

    }

  }

  /**
    * Normalize data and preserve labels
    * @param data
    * @param transformer
    * @return
    */
  def prepareData(data: RDD[Array[String]], transformer: KddTransformer) = {

    val unlabeledData = data.map { row =>
      row.dropRight(1)
    }
    val labels = data.map(_.takeRight(1)(0))

    val normData = transformer.transform(unlabeledData)

    labels zip normData
  }


}
