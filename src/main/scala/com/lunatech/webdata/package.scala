package com.lunatech

import org.apache.commons.io.FileUtils


/**
  * Common utilities
  */
package object webdata {

  def removeDir(path: String) = {
    val dir = new java.io.File(path)
    if (dir exists)
      FileUtils.deleteDirectory(dir)
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

}
