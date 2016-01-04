package com.lunatic.mlx.kddcup99.stationary

import com.lunatic.mlx.kddcup99._
import com.lunatic.mlx.kddcup99.transformers._
import org.apache.spark.SparkContext


/**
  * Educate the KddTransformer for different normalization algorithms
  */
object EducateTransformers extends SparkRunnable {

  def main(args: Array[String]) = {

    DefaultSparkRunner(this.getClass.getName, args).run(this)

  }

  def run(implicit sc: SparkContext, appConf: Configuration) = {

    // Normalization algorithms... it boils down to a mix actually, as in the end L1Norm will be applied anyway
    val normalizers = List(DataNormalizer.L2NormV1)

    // The following are the index of the columns containing discrete data, including the label column
    val symboliColumns = List(1, 2, 3, 6, 11, 20, 21)

    // The input data analysis showd column 19 as having just one value, so we can remove it as we can learn nothing from it.
    // This also helps with the L2normalization since it is better not to dive by zero
    val removableColumns = List(19)

    val labeledData = sc.textFile(appConf.inputTrainingData).map(_.split(",")).cache

    val unlabeledData = labeledData.map { row =>
      row.dropRight(1)
    }

    normalizers.foreach { normalizer =>

      val transformer = KddTransformer(symboliColumns, normalizer, Some(removableColumns)).learn(unlabeledData)
      com.lunatic.mlx.removeHdfsFile(appConf.transformerModelPath(normalizer))
      saveObjectToFile(transformer, appConf.transformerModelPath(normalizer))

      // coolMyCPU(60)
    }

  }

}
