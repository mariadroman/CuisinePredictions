package com.lunatech.webdata.cuisine

import com.lunatech.webdata._
import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.json4s._
import org.json4s.jackson.JsonMethods._

object ImportDataModel {

  val dataPath: String = Configuration.inputTrainingData

  def main(args: Array[String]) = {

    val conf = new SparkConf(true).setAppName("ImportDataModel").
      setMaster("local[*]").
      set("spark.driver.memory", "16g").
      set("spark.executor.memory", "16g").
      set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    val sc = new SparkContext(conf)

    val rawData: RDD[(LongWritable, Text)] =
      sc.newAPIHadoopFile[LongWritable, Text, CustomLineInputFormat](dataPath)

    val recipes = rawDataToRecipes(rawData)

    val cuisines = recipes.map(r => r.cuisine).distinct.cache
    val cuisineList = cuisines.collect.toSeq.sorted

    val ingredients = recipes.flatMap(r => r.ingredients).distinct.cache
    val ingredientsList = ingredients.collect.toSeq.sorted

    val cuisineToIndex = cuisineList.
      zip(0 until cuisineList.size).toMap

    val ingredientsIndices = (0 until ingredientsList.size)

    val ingredientToIndex = ingredientsList.zip(ingredientsIndices).toMap

    val data = recipes.map { r =>
      val label = cuisineToIndex(r.cuisine)
      val values = r.ingredients.map(i => 1.0).toArray
      val indices = r.ingredients.map(ingredientToIndex(_)).toArray
      val vector = Vectors.sparse(ingredientToIndex.size, indices, values)
      LabeledPoint(label, vector)
    }

    // Save all necessary files to be used in later steps
    removeDir(Configuration.dataPath)
    data.saveAsTextFile(Configuration.dataPath)

    removeDir(Configuration.recipesPath)
    recipes.saveAsObjectFile(Configuration.recipesPath)

    removeDir(Configuration.ingredientsPath)
    sc.parallelize(ingredientToIndex.toSeq).saveAsObjectFile(Configuration.ingredientsPath)

    removeDir(Configuration.cuisinesPath)
    sc.parallelize(cuisineToIndex.toSeq).saveAsObjectFile(Configuration.cuisinesPath)

  }

  def rawDataToRecipes(rawData: RDD[(LongWritable, Text)]): RDD[Recipe] = {
    implicit lazy val formats = org.json4s.DefaultFormats
    rawData.map(x => parse(x._2.toString)).map(
      json => {
        val id = (json \ "id").extract[Int]
        val cuisine = (json \ "cuisine").extractOrElse[String]("unknown").toLowerCase
        val ingredients = (json \ "ingredients").extractOrElse[List[String]](List()).map(_.toLowerCase)
        Recipe(id, cuisine, ingredients)
      }
    )
  }


}

