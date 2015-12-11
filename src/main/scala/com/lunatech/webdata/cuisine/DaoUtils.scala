package com.lunatech.webdata.cuisine

import java.io._

import com.lunatech.webdata._
import com.lunatech.webdata.cuisine.mllib.{MulticlassMetrix, Model}
import org.apache.spark.SparkContext
import org.apache.spark.mllib.classification.{LogisticRegressionModel, NaiveBayesModel}
import org.apache.spark.mllib.tree.model.{DecisionTreeModel, RandomForestModel}
import org.apache.spark.rdd.RDD

import scala.reflect.{ClassTag, Manifest}
import scala.util.Try

/**
 * VERY UGLY, but for now it works.
 *
 * This might be a good type classes implementation idea.
 */
object DaoUtils {

  def saveModel(model: Model[_])(implicit sc: SparkContext, configuration: Configuration) = {
    val path = getPath(model)
    //    removeHdfsFile(path)
    //    model.save(path)
    removeLocalFile(path)
    saveAsLocalObject(model, path)
  }

  def saveMetrix(model: Model[_], metrics: MulticlassMetrix)(implicit sc: SparkContext, configuration: Configuration) = {
    val path = getPath(model) + ".metrics"
    removeLocalFile(path)
    saveAsLocalJsonFile(metrics, path)
  }

  def loadMetrix(model: Model[_])(implicit configuration: Configuration):
  Try[MulticlassMetrix] = {
    val path = getPath(model) + ".metrics"
    loadFromLocalJsonFile[MulticlassMetrix](path)
  }

  def getPath(model: Model[_])(implicit configuration: Configuration) = model.self match {
    case m1: LogisticRegressionModel => configuration.logisticRegPath
    case m2: NaiveBayesModel => configuration.naiveBayesPath
    case m2: DecisionTreeModel => configuration.decisionTreePath
    case m2: RandomForestModel => configuration.randomForestPath
  }

  def saveAsSparkObject[T: ClassTag](data: Seq[T], path: String)(implicit sc: SparkContext) = {
    sc.parallelize(data).saveAsObjectFile(path)
  }

  def loadAsSparkObject[T: ClassTag](path: String)(implicit sc: SparkContext): RDD[T] = {
    sc.objectFile[T](path)
  }

  def saveAsLocalObject[T](data: T, path: String): Boolean = {
    removeLocalFile(path)
    val file = new File(path)
    file.getParentFile.mkdirs()
    val result = Try(new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file))))
      .map { os =>
        os.writeObject(data); os.close
      }
    result.isSuccess
  }

  def loadFromLocalObject[T](path: String): Try[T] = {
    Try(new ObjectInputStream(new BufferedInputStream(new FileInputStream(path))))
      .map { p =>
        val data = p.readObject().asInstanceOf[T]
        p.close
        data
      }
  }


  def saveAsLocalJsonFile[T <: scala.AnyRef](that: T, path: String): Boolean = {
    removeLocalFile(path)
    import org.json4s._
    import org.json4s.jackson.Serialization
    import org.json4s.jackson.Serialization._
    implicit val formats = Serialization.formats(NoTypeHints)
    val file = new File(path)
    file.getParentFile.mkdirs()
    val writer = new BufferedWriter(new FileWriter(file, true))
    val result = Try(write(that, writer)).isSuccess
    Try(writer.close)
    result
  }


  def loadFromLocalJsonFile[T](path: String)(implicit mf: Manifest[T]): Try[T] = {

    import org.json4s._
    import org.json4s.jackson.Serialization
    import org.json4s.jackson.Serialization._
    implicit val formats = Serialization.formats(NoTypeHints)
    val reader = new FileReader(path)
    val result = Try(read[T](reader))
    Try(reader.close)
    result
  }
}
