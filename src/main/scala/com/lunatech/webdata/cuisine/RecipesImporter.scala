package com.lunatech.webdata.cuisine

import com.lunatech.webdata._
import com.lunatech.webdata.cuisine.model.Recipe
import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.json4s._
import org.json4s.jackson.JsonMethods._

/**
  * Import the recipes from a given file into Spark
  */
object RecipesImporter {

  /**
    * Import recipes and save them as a Spark RDD
    * @param args
    */
  def main(args: Array[String]) = {

    val defConf = new SparkConf(true)
    val conf = defConf.setAppName("CuisineRecipesImportData").
      setMaster(defConf.get("spark.master",  "local[*]"))

    implicit val sc = new SparkContext(conf)
    implicit val configuration = Configuration(args)

    val recipes = importFrom(configuration.inputTrainingData)

    removeFile(configuration.recipesPath)
    recipes.saveAsObjectFile(configuration.recipesPath)

  }

  def importFrom(path: String)(implicit sc: SparkContext): RDD[Recipe] = {

    val rawData: RDD[(LongWritable, Text)] =
      sc.newAPIHadoopFile[LongWritable, Text, CustomLineInputFormat](path)

    implicit lazy val formats = org.json4s.DefaultFormats

    rawData.map(x => parse(x._2.toString)).map(
      json => {
        val id = (json \ "id").extract[Int]
        // TODO: I know, I know, cuisine should be Option[String]... you do it!
        val cuisine = (json \ "cuisine").extractOrElse[String]("unknown").toLowerCase
        val ingredients = (json \ "ingredients").extractOrElse[List[String]](List()).map(_.toLowerCase)
        Recipe(id, cuisine, ingredients)
      }
    )
  }

}

