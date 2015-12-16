package com.lunatech.webdata.cuisine.mllib.transformers

import com.lunatech.webdata.cuisine.mllib.FlowData
import org.apache.spark.mllib.feature.ChiSqSelector
import org.apache.spark.mllib.regression.LabeledPoint

/**
 *
 */
case class ChiSqSelectorTransformer(topFeaturesRatio: Double) extends Transformer[FlowData] {

  require(topFeaturesRatio < 1.0 && topFeaturesRatio > 0)

  override def transform(flowData: FlowData): FlowData = {

    val numTopFeatures = (flowData.indexToFeature.size * topFeaturesRatio).toInt

    // Create ChiSqSelector that will select top numTopFeatures features
    val selector = new ChiSqSelector(numTopFeatures)

    // Create ChiSqSelector model (selecting features)
    val transformer = selector.fit(flowData.data)

    // Filter the top numTopFeatures features from each feature vector
    val filteredData = flowData.data.map { lp =>
      LabeledPoint(lp.label, transformer.transform(lp.features))
    }

    val filteredFeaturesIndex = flowData.featureToIndex
      .filter { case (feature, index) =>
        transformer.selectedFeatures.contains(index)
      }

    FlowData(filteredData, flowData.labelToIndex, filteredFeaturesIndex)

  }
}
