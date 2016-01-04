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
object SplitTrainingData extends SparkRunnable {

  def main(args: Array[String]) = {

    DefaultSparkRunner(this.getClass.getName, args).run(this)

  }

  def run(implicit sc: SparkContext, appConf: Configuration) = {

     val analyser = loadObjectFromFile[InputAnalyzer[String]](appConf.analyzerModelPath).get

    //TODO Ugly!!! Redo!
    val labelsCount = analyser.distinctCountByColumn.get.get(41).get

    val rawData = sc.textFile(appConf.inputTrainingData).map(_.split(",")).cache

    val distinctLabels = labelsCount.map(_._1).toSeq

    val labeledData = splitEvenlyByLabel(0.85, distinctLabels, rawData)

    // Save un-normalized training set
    com.lunatic.mlx.removeHdfsFile(appConf.trainDataPath)
    labeledData._1.saveAsObjectFile(appConf.trainDataPath)

    // Save un-normalized test set
    com.lunatic.mlx.removeHdfsFile(appConf.testDataPath)
    labeledData._2.saveAsObjectFile(appConf.testDataPath)


  }

  /**
    * Split the data by the given ratio for each label
    * @param ratio the ratio of the first element of the tuple result
    * @param distinctLabels
    * @param data
    * @return a tuple of data randomly split by the same ratio for each label
    */
  def splitEvenlyByLabel(ratio: Double, distinctLabels: Seq[String], data: RDD[Array[String]]) ={
    distinctLabels.map { label =>
      val splits = data.filter(_.last == label).randomSplit(Array(ratio, 1-ratio))
      if(splits.size == 1)
        (splits(0), splits(0))
      else
        (splits(0), splits(1))
    }.reduce { (x1, x2) => (x1._1.union(x2._1), x1._2.union(x2._2)) }
  }


}
