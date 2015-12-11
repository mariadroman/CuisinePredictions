package com.lunatech.webdata.cuisine.mllib

import com.lunatech.webdata.cuisine.DaoUtils
import org.apache.spark.SparkContext
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD

/** s
  * This is the data that is passed along in the flow.
  *
  * It contains all necessary data for training and essential data for prediction
  * (the index to label and index to feature mappings)
  */
case class FlowData(data: RDD[LabeledPoint],
                    labelToIndex: Map[String, Int],
                    featureToIndex: Map[String, Int]) {

  // TODO: I have some funky ideas for this class... I smell a monad...

  import FlowData._

  def indexToLabel = labelToIndex.map(r => r._2 -> r._1)

  def indexToFeature = featureToIndex.map(r => r._2 -> r._1)

  def map(f: (FlowData) => FlowData): FlowData = f(this)

  // TODO FlowData DAO should be transparent (e.g. dealt with by third party)
  def save(rootPath: String)(implicit sc: SparkContext) = {

//    removeHdfsFile(rootPath + dataDir)
//    data.saveAsTextFile(rootPath + dataDir)
    DaoUtils.saveAsLocalObject(data.collect(), rootPath + dataDir)

//    removeHdfsFile(rootPath + labelsDir)
//    DaoUtils.toJsonFile(labelToIndex, rootPath + labelsDir)
    DaoUtils.saveAsLocalObject(labelToIndex, rootPath + labelsDir)

//    removeHdfsFile(rootPath + featuresDir)
//    DaoUtils.toJsonFile(featureToIndex, rootPath + featuresDir)
    DaoUtils.saveAsLocalObject(featureToIndex, rootPath + featuresDir)

  }

  def setData(newData: RDD[LabeledPoint]): FlowData =
    FlowData(newData, labelToIndex, featureToIndex)

  def setLabelToIndex(newLabelToIndex: Map[String, Int]): FlowData =
    FlowData(data, newLabelToIndex, featureToIndex)

  def setFeatureToIndex(newFeatureToIndex: Map[String, Int]): FlowData =
    FlowData(data, labelToIndex, newFeatureToIndex)

}

object FlowData {

  private val dataDir = "/data"
  private val labelsDir = "/labels"
  private val featuresDir = "/features"

  // TODO FlowData DAO should be transparent (e.g. dealt with by third party)
  def load(rootPath: String)(implicit sc: SparkContext): FlowData = {
//    FlowData(
//      MLUtils.loadLabeledPoints(sc, rootPath + dataDir),
//      sc.objectFile[(String, Int)](rootPath + labelsDir).collect().toMap,
//      sc.objectFile[(String, Int)](rootPath + featuresDir).collect().toMap
//    )
    FlowData(
      sc.parallelize(DaoUtils.loadFromLocalObject[Array[LabeledPoint]](rootPath + dataDir).get),
      DaoUtils.loadFromLocalObject[Map[String, Int]](rootPath + labelsDir).get,
      DaoUtils.loadFromLocalObject[Map[String, Int]](rootPath + featuresDir).get
    )
  }

}
