package com.lunatic.mlx.kddcup99

import com.lunatic.mlx.kddcup99.transformers.DataNormalizer

/**
  * Some basic paths configuration
  */
case class Configuration(args: Array[String]) {

  // TODO: Refactor and improve

  val appPrefix = "OT_KDD_"

  val argsMap = args.map(_.split("=")).map(x => (x(0), x(1))).toMap

  val inputTrainingData = argsMap.getOrElse("app.input.file.training", "/tmp/KDD-Cup-1999/kddcup.data")
//  val inputTrainingData = argsMap.getOrElse("app.input.file.training", "/tmp/KDD-Cup-1999/kddcup.data_10_percent")

//  val inputTestingData = argsMap.getOrElse("app.input.file.test", "/tmp/KDD-Cup-1999/kddcup.testdata.unlabeled")
  val inputTestingData = argsMap.getOrElse("app.input.file.test", "/tmp/KDD-Cup-1999/corrected")

  val workPath = argsMap.getOrElse("app.wip.path", "/tmp/KDD-Cup-1999/wip")

  val labelsCountPath = workPath + "/labels_count"

  val analyzerModelPath = workPath + "/analysis"

  val outputPath = argsMap.getOrElse("app.output.file.predictions", "/tmp/KDD-Cup-1999/out")

  val es_index_auto_create = argsMap.getOrElse("es.index.auto.create", "true")

  val es_nodes = argsMap.getOrElse("es.nodes","localhost")

  val es_port = argsMap.getOrElse("es.port","9200")

  val es_index = argsMap.getOrElse("es.index","kddcup99/raw")


  val trainDataPath = workPath + "/data_train"

  val testDataPath = workPath + "/data_test"

  def trainDataPath(normalizer: String = DataNormalizer.L2NormV1): String = trainDataPath + "_" + normalizer

  def testDataPath(normalizer: String = DataNormalizer.L2NormV1): String = testDataPath + "_" + normalizer

  def transformerModelPath(normalizer: String = DataNormalizer.L2NormV1): String = workPath + "/transformer_" + normalizer

}


