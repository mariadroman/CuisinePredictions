package com.lunatic.mlx

import java.io.{BufferedOutputStream, PrintWriter}

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

  /**
   * Create a stub file that can be used to save models and results
   * @param k
   * @param epsilon
   * @param maxIterations
   * @param runs
   * @param norm Just an arbitrary strign suggesting the normalization algorithm used
   * @return
   */
  def stubFilePath(k: Int, epsilon: Double, maxIterations: Int, runs: Int, norm: String)(implicit appConf: Configuration) =
    f"${appConf.outputPath}/kmeans_$epsilon%.0E_$k%03d_$maxIterations%04d_$runs%02d_$norm".
      replaceAll("\\+", "")

}
