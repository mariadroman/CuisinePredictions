package com.lunatech.webdata.cuisine.ml

import com.lunatech.webdata.CustomLineInputFormat
import com.lunatech.webdata.cuisine.{Configuration, ImportDataModel}
import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.classification.LogisticRegression
import org.apache.spark.ml.feature.{HashingTF, IndexToString, StringIndexer, VectorIndexer}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}

/**
 *
 */
object PredictionPipeline {

  def main(args: Array[String]) = {


    val conf = new SparkConf(true).setAppName("CuisineTrainingDecisionTrees")
      .setMaster("local[*]")
      .set("spark.driver.memory", "8g")
      .set("spark.executor.memory", "8g")
      // .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")

    conf.getAll.foreach(println)

    val sc = new SparkContext(conf)

    val sqc = new SQLContext(sc)

    val rawData: RDD[(LongWritable, Text)] =
      sc.newAPIHadoopFile[LongWritable, Text, CustomLineInputFormat](Configuration.inputTrainingData)

    val recipes = ImportDataModel.rawDataToRecipes(rawData)

    val data = sqc.createDataFrame(recipes)

    val ingredientsHasher = new HashingTF()
      .setInputCol("ingredients")
      .setOutputCol("features")

    // Index labels, adding metadata to the label column.
    val labelIndexer = new StringIndexer()
      .setInputCol("cuisine")
      .setOutputCol("label")
      .fit(data)

    // Automatically identify categorical features, and index them.
    val featureIndexer =
      new VectorIndexer()
      .setInputCol("features")
      .setOutputCol("indexedFeatures")
      .setMaxCategories(20) // features with > 4 distinct values are treated as continuous

    // Train a DecisionTree model.
    val trainer = new LogisticRegression()
      .setLabelCol("label")
      .setFeaturesCol("features")

    val labelConverter = new IndexToString()
      .setInputCol("prediction")
      .setOutputCol("predictedLabel")
      .setLabels(labelIndexer.labels)

    val pipeline = new Pipeline()
      .setStages(Array(labelIndexer,
        ingredientsHasher,
//        featureIndexer,
        trainer,
        labelConverter))


    println("----------------")

    println(s"Pipeline Params: \n${pipeline.explainParams()}")

    // Split the data into training and test sets (5% held out for testing)
    val Array(trainingData, testData) = data.randomSplit(Array(0.4, 0.6))

    val model = pipeline.fit(trainingData)

    println("----------------")

    println(s"Model Params: \n${model.explainParams()}")

    val predictions = model.transform(testData)


    println("---------------------------------")
    println("PREDICTIONS")
    predictions.printSchema()

    val collectedPredictions = predictions.select("cuisine", "predictedLabel", "prediction", "label")
      .collect
    println(f"${"Actual"}%30s  |  ${"Predicted"}")
    println(f"${"---------------------------"}%30s  |  ${"---------------------------"}")
    collectedPredictions
      .take(20)
      .foreach(r => println(f"${r.getAs[String]("cuisine")}%30s  |  ${r.getAs[String]("predictedLabel")}"))


    val accuracy = collectedPredictions
      .filter(r => r.getAs[Double]("prediction") == r.getAs[Double]("label")).size.toDouble / predictions.count()

    println("---------------------------------")
    println(f"Accuracy = ${accuracy * 100}%.4f%%")
    println(f"Error    = ${(1 - accuracy) * 100}%.4f%%")

  }
}
