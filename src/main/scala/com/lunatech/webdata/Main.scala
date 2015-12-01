import com.lunatech.webdata.{CustomLineInputFormat}
import org.apache.hadoop.io.{Text, LongWritable}
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.json4s._
import org.json4s.jackson.JsonMethods._
import java.io._

import org.apache.spark.mllib.tree.DecisionTree
import org.apache.spark.mllib.tree.model.DecisionTreeModel
import org.apache.spark.mllib.util.MLUtils

object Cuisine {

  val dataPath: String = "ML/train.json"

  def main(args: Array[String]) = {

    val conf = new SparkConf().setAppName("Sparkles").setMaster("local[*]")//.registerKryoClasses(Array(Class.forName("org.apache.hadoop.io.LongWritable"),Class.forName("org.apache.hadoop.io.Text")))
    val sc = new SparkContext(conf)
    val lines = sc.textFile(dataPath)
    val rawData: RDD[(LongWritable, Text)] =
      sc.newAPIHadoopFile[LongWritable, Text, CustomLineInputFormat](dataPath)
    //rawData.saveAsTextFile("ParsedData")

    implicit lazy val formats = org.json4s.DefaultFormats

    val cuisineList = rawData.map(x => (parse(x._2.toString) \ "cuisine").extract[String]).collect().groupBy(x => x).map(x => x._1).toSeq.sorted

    val ingredientsList = rawData.map( x => (parse(x._2.toString) \ "ingredients").extract[List[String]]).collect().toList.flatMap(x => x).groupBy(x => x).map(x => x._1).toSeq.sorted

    val recipes = rawData.map( x => parse(x._2.toString)).map(
    json => {
      implicit lazy val formats = org.json4s.DefaultFormats
      val id = (json \ "id").extract[Int]
      val cuisine = (json \ "cuisine").extract[String]
      val ingredients = (json \ "ingredients").extract[List[String]]
      Recipe(id, cuisine, ingredients)
      //json.extract[Recipe]
    }
    ).collect().toList


    val list1 = List.range(1,cuisineList.size+1).toSeq
    //println(list1.size)
    val cuisinesMap = list1.map( x => cuisineList(x-1) -> x).toMap


    val list2 = List.range(1, ingredientsList.size+1).toSeq
    //println(list2.size)
    val ingredientsMap = list2.map( x => ingredientsList(x-1) -> x).toMap

    val pw = new PrintWriter(new File("libSVM.txt"))
    recipes.foreach(x => pw.write(recipeToString( x, cuisinesMap, ingredientsMap)+"\n"))

    pw.close


//    // Load and parse the data file.
//    val data = MLUtils.loadLibSVMFile(sc, "libSVM.txt")
//    // Split the data into training and test sets (30% held out for testing)
//    val splits = data.randomSplit(Array(0.7, 0.3))
//    val (trainingData, testData) = (splits(0), splits(1))
//
//    // Train a DecisionTree model.
//    //  Empty categoricalFeaturesInfo indicates all features are continuous.
//    val categoricalFeaturesInfo = Map[Int, Int]()
//    val impurity = "variance"
//    val maxDepth = 5
//    val maxBins = 32
//
//    val model = DecisionTree.trainRegressor(trainingData, categoricalFeaturesInfo, impurity,
//      maxDepth, maxBins)
//
//    // Evaluate model on test instances and compute test error
//    val labelsAndPredictions = testData.map { point =>
//      val prediction = model.predict(point.features)
//      (point.label, prediction)
//    }
//
//    val testMSE = labelsAndPredictions.map{ case(v, p) => math.pow((v - p), 2)}.mean()
//    println("Test Mean Squared Error = " + testMSE)
//    println("Learned regression tree model:\n" + model.toDebugString)
//
//    // Save and load model
//    model.save(sc, "myModelPath")
//    val sameModel = DecisionTreeModel.load(sc, "myModelPath")


    // Load and parse the data file.
    val data = MLUtils.loadLibSVMFile(sc, "libSVM.txt")
    // Split the data into training and test sets (30% held out for testing)
    val splits = data.randomSplit(Array(0.95, 0.05))
    val (trainingData, testData) = (splits(0), splits(1))

    // Train a DecisionTree model.
    //  Empty categoricalFeaturesInfo indicates all features are continuous.
    val numClasses = 21
    val categoricalFeaturesInfo = Map[Int, Int]()
    val impurity = "gini"
    val maxDepth = 10
    val maxBins = 32

    val model = DecisionTree.trainClassifier(trainingData, numClasses, categoricalFeaturesInfo,
      impurity, maxDepth, maxBins)

    // Evaluate model on test instances and compute test error
    val labelAndPreds = testData.map { point =>
      val prediction = model.predict(point.features)
      (point.label, prediction)
    }
    val testErr = labelAndPreds.filter(r => r._1 != r._2).count.toDouble / testData.count()
    println("Test Error = " + testErr)
    println("Learned classification tree model:\n" + model.toDebugString)

    // Save and load model
    model.save(sc, "myModelPath")
    val sameModel = DecisionTreeModel.load(sc, "myModelPath")
  }


  def recipeToString(recipe: Recipe, cuisineMap: Map[String,Int], ingredientsMap: Map[String, Int]): String = {
    val libSVMStr = new StringBuilder(cuisineMap.get(recipe.cuisine).get + " ")
    recipe.ingredients.toSet.toSeq.sorted.foreach(x => libSVMStr.append(ingredientsMap.get(x).get+ ":" + 1 + " "))
    libSVMStr.toString()
  }

}

case class Recipe(id: Int, cuisine: String, ingredients: List[String])
