package com.lunatech.webdata.cuisine.mllib.transformers

import com.lunatech.webdata.cuisine.Configuration
import com.lunatech.webdata.cuisine.mllib.FlowData
import org.apache.spark.{SparkContext, SparkConf}

/**
 *
 */
class TfIdfTransformer extends Transformer[FlowData] {

  override def transform(flowData: FlowData): FlowData = {

    flowData

  }
}

object TfIdfTransformer {

  def main(args: Array[String]) = {

    val defConf = new SparkConf(true)
    val conf = defConf.setAppName("CuisineRecipesBuildModels").
      setMaster(defConf.get("spark.master",  "local[*]"))

    implicit val sc = new SparkContext(conf)
    implicit val configuration = Configuration(args)
    // Load the flow data
    val flowData = FlowData.load(configuration.dataPath)

    new TfIdfTransformer().transform(flowData)

  }
}
