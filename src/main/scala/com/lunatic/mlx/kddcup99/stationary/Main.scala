package com.lunatic.mlx.kddcup99.stationary

import com.lunatic.mlx.kddcup99.DefaultSparkRunner

/**
  * This is mainly useful to show the flow when starting from scratch
  */
object Main {

  def main(args: Array[String]) = {

    val tasks = List(
      AnalyseInputData,
      EducateTransformers,
      SplitTrainingData,
      NormalizeTrainingData,
      TrainKMeans,
      PredictKMeans)

    DefaultSparkRunner(this.getClass.getName, args).run(tasks)

  }
}
