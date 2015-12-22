package com.lunatic.mlx.kddcup99

/**
 *
 */
object Main {

  def main(args: Array[String]) = {

    val tasks = List(ImportData, TrainKMeans)

    DefaultSparkRunner(this.getClass.getName, args).run(tasks)

  }
}
