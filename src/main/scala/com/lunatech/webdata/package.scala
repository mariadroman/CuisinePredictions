package com.lunatech

import org.apache.commons.io.FileUtils

/**
 *
 */
package object webdata {

  def removeDir(path: String) = {
    val dir = new java.io.File(path)
    if(dir exists)
      FileUtils.deleteDirectory(dir)
  }

}
