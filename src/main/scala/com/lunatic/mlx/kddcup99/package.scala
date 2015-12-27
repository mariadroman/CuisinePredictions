package com.lunatic.mlx

import java.io._

import org.apache.hadoop.fs.{Path, FileSystem}

import scala.util.Try

/**
 *
 */
package object kddcup99 {

  /**
   * Save a sequence of Strings as lines in an HDFS file
   * @param rez
   * @param path
   * @return
   */
  def saveLinesToFile(rez: Seq[String], path: String) = {
    // Create the HDFS file system handle
    val hdfs = FileSystem.get(new org.apache.hadoop.conf.Configuration())
    // Create a writer
    val writer = new PrintWriter(new BufferedOutputStream(hdfs.create(new Path(path), true)), true)
    //write each line
    rez.foreach(line => Try(writer.print(line + "\n")))
    // Close the streams
    Try(writer.close())
  }

  def saveObjectToJsonFile[T <: scala.AnyRef](that: T, path: String): Boolean = {
    import org.json4s._
    import org.json4s.jackson.Serialization
    import org.json4s.jackson.Serialization._
    implicit val formats = Serialization.formats(NoTypeHints)

    // Create the HDFS file system handle
    val hdfs = FileSystem.get(new org.apache.hadoop.conf.Configuration())
    val writer = new PrintWriter(new BufferedOutputStream(hdfs.create(new Path(path), true)), true)
    val result = Try(write(that, writer)).isSuccess
    Try(writer.close)
    result
  }


  def loadObjectFromJsonFile[T](path: String)(implicit mf: Manifest[T]): Try[T] = {

    import org.json4s._
    import org.json4s.jackson.Serialization
    import org.json4s.jackson.Serialization._
    implicit val formats = Serialization.formats(NoTypeHints)

    // Create the HDFS file system handle
    val hdfs = FileSystem.get(new org.apache.hadoop.conf.Configuration())
    val reader = new InputStreamReader(new BufferedInputStream(hdfs.open(new Path(path))))
    val result = Try(read[T](reader))
    Try(reader.close)
    result
  }

  /**
    * Generate an experiment if based on the experiment parameters
    * @param k
    * @param epsilon
    * @param maxIterations
    * @param runs
    * @param norm Just an arbitrary string suggesting the normalization algorithm used
    * @return experimentId
    */
  def experimentId(k: Int, epsilon: Double, maxIterations: Int, runs: Int, norm: String): String =
    f"$epsilon%.0E_$k%03d_$maxIterations%04d_$runs%02d_$norm".
      replaceAll("\\+", "")

  def experimentId(params: KMeansParams): String =
    experimentId(params.k, params.epsilon, params.maxIterations, params.runs, params.norm)

  /**
    * Create a stub file that can be used to save models and results
    * @param params
    * @param appConf
    * @return
    */
  def stubFilePath(params: KMeansParams)(implicit appConf: Configuration) =
    f"${appConf.outputPath}/kmeans_${experimentId(params)}"

}
