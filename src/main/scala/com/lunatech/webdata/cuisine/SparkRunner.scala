package com.lunatech.webdata.cuisine

import org.apache.spark.SparkContext

/**
 * Trivial trait for running basic Spark apps.
 *
 * The run() returns Unit, so just side effects... sorry
 */
trait SparkRunner {

  def run(implicit sc: SparkContext, configuration: Configuration)

}
