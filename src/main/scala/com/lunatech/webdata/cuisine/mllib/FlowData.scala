package com.lunatech.webdata.cuisine.mllib

import com.lunatech.webdata._
import org.apache.spark.SparkContext
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.rdd.RDD

/**s
 * This is the data that is passed along in the flow.
  *
  * It contains all necessary data for training and essential data for prediction
  * (the index to label and index to feature mappings)
 */
case class FlowData(data: RDD[LabeledPoint],
                    labelToIndex: Map[String, Int],
                    featureToIndex: Map[String, Int]) {

  import FlowData._

  lazy val indexToLabel = labelToIndex.map(r => r._2 -> r._1)
  lazy val indexToFeature = featureToIndex.map(r => r._2 -> r._1)


  // TODO FlowData DAO should be transparent (e.g. dealt with by third party)
  def save(rootPath: String)(implicit sc: SparkContext) = {

    removeDir(rootPath + dataDir)
    data.saveAsTextFile(rootPath + dataDir)

    removeDir(rootPath + labelsDir)
    sc.parallelize(labelToIndex.toSeq).saveAsObjectFile(rootPath + labelsDir)

    removeDir(rootPath + featuresDir)
    sc.parallelize(featureToIndex.toSeq).saveAsObjectFile(rootPath + featuresDir)

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
    FlowData(
      MLUtils.loadLabeledPoints(sc, rootPath + dataDir),
      sc.objectFile[(String, Int)](rootPath + labelsDir).collect().toMap,
      sc.objectFile[(String, Int)](rootPath + featuresDir).collect().toMap
    )
  }

}
