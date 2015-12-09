package com.lunatech

import org.apache.commons.io.FileUtils


/**
  * Common utilities
  */
package object webdata {

  def removeFile(path: String) = {
    val file = new java.io.File(path)
    if (file.exists && file.isDirectory)
      FileUtils.deleteDirectory(file)
    else
      file.delete()
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
