package com.lunatech.webdata.cuisine.mllib.transformers

import com.lunatech.webdata.cuisine.{RecipesImporter, Configuration}
import com.lunatech.webdata.cuisine.mllib.FlowData
import com.lunatech.webdata.cuisine.model.Recipe
import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.mllib.feature.{IDF, HashingTF}
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import org.apache.spark.mllib.linalg.Vector

/**
   * Read data from a json file, parse it, transform it into a bag of recipes and produce a FlowData object
   */
object RecipesHasher extends Transformer[RDD[Recipe]] {


  def main(args: Array[String]) = {

    val defConf = new SparkConf(true)
    val conf = defConf.setAppName("CuisineRecipesBuildModels").
      setMaster(defConf.get("spark.master",  "local[*]"))

    implicit val sc = new SparkContext(conf)
    implicit val configuration = Configuration(args)
    // Load the flow data
    val flowData = FlowData.load(configuration.dataPath)

    // import the recipes
    val recipes = RecipesImporter.importFrom(configuration.inputTrainingData)

    RecipesHasher.transform(recipes)

    Unit

  }

    def transform(recipes: RDD[Recipe]): FlowData = {



      // Normally we should keep this an RDD, but we have a small list here
      val cuisines = recipes.map(r => r.cuisine)
        .map((_, 1)).reduceByKey(_+_).sortBy(_._2, false) // sort the cuisines by the number of recipes descending
        .collect
      val cuisinesList = cuisines.map(_._1).toSeq

      // Normally we should keep this an RDD, but we have a small list here
      val ingredients = recipes.flatMap(r => r.ingredients)
        .map((_, 1)).reduceByKey(_+_)
         //  .filter(_._2 != 1) // Should we filter out the "irrelevant features"?
        .sortBy(_._1).sortBy(_._2, false) // sort the ingredients by occurrences descending
        .collect
      val ingredientsList = ingredients.map(_._1).toSeq

      val cuisineToIndex = cuisinesList
        .zipWithIndex.toMap




      val hashingTF = new HashingTF()

      val terms = recipes.map(r => r.ingredients)
      val features = hashingTF.transform(terms).cache

      val labels = recipes.map(r => cuisineToIndex(r.cuisine))

      val data = labels.zip(features).map(x => LabeledPoint(x._1, x._2))

      val ingredientToIndex = ingredientsList
        .map(in => (in, hashingTF.indexOf(in))).toMap


      println("------------")
      ingredientToIndex.toSeq.sortBy(_._2).splitAt(20)._1.foreach(println)
      ingredientToIndex.toSeq.sortBy(_._2).reverse.splitAt(20)._1.foreach(println)
      println("------------")



      val fd = FlowData(data, cuisineToIndex, ingredientToIndex)

      recipes.take(10).zip(data.take(10)).foreach{ case (r, lp) =>
        println(r)
        println(lp)
        println(f"${lp.label.toInt}%3d | ${fd.indexToLabel(lp.label.toInt)}")
        val features = lp.features.toArray
        (0 until features.size).foreach { i =>
          if(features(i) > 0) {
            println("  - " + fd.indexToFeature(i))
          }
        }
      }

      val idf = new IDF(minDocFreq = 2).fit(features)
      val tfidf: RDD[Vector] = idf.transform(features)

      val ndata = labels.zip(tfidf).map(x => LabeledPoint(x._1, x._2))
      println("------------")
      ndata.take(10).foreach(println)
      println("------------")

      FlowData(data, cuisineToIndex, ingredientToIndex)


    }

  }

