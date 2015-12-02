package com.lunatech.webdata.cuisine.mllib

import com.lunatech.webdata._
import org.apache.spark.SparkContext
import org.apache.spark.mllib.evaluation.MulticlassMetrics

/**
 * Trainer interface for unifying the MLLib.
 */
trait Trainer[M] {

  def train(flowData: FlowData)(implicit sc: SparkContext): Model[M]

  def trainEvaluate(flowData: FlowData, trainingQuota: Double = 0.8)
                   (implicit sc: SparkContext): (Model[M], MulticlassMetrics, Trainer.Runtime) = {

    val splits = flowData.data.randomSplit(Array(trainingQuota, 1 - trainingQuota))
    val (trainingData, testData) = (splits(0), splits(1))

    val (model, runtime) = timeCode {
      train(flowData.setData(trainingData))
    }

    val predictionAndLabels = testData.map { point =>
      val prediction = model.predict(point.features)
      (prediction, point.label)
    }
    val metrics = new MulticlassMetrics(predictionAndLabels)

    (model, metrics, runtime)

  }
}

object Trainer {

  type Runtime = Double

}
