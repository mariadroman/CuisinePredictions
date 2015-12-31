package com.lunatic

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs._
import org.apache.commons.io.FileUtils


/**
  * Common utilities
  */
package object mlx {

  def removeHdfsFile(path: String) = {
    val hdfs = FileSystem.get(new Configuration())
    val workingPath = new Path(path)
    hdfs.delete(workingPath,true) // delete recursively
  }

  def removeLocalFile(path: String) = {
    FileUtils.deleteQuietly(new java.io.File(path))
  }

  /**
   * Run a block and return the block result and the runtime in millis
   * @param block
   * @return
   */
  def timeCode[T](block : => T): (T, Long) = {
    val start = new java.util.Date
    val result = block
    val runtime = (new java.util.Date).toInstant.toEpochMilli - start.toInstant.toEpochMilli
    (result, runtime)
  }

  def coolMyCPU(seconds: Int) = {
    println(s"Sleeping for $seconds seconds... (cooling of my CPU)")
    Thread.sleep(seconds * 1000)
  }

}
