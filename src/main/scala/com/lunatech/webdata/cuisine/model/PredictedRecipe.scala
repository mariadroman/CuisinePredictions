package com.lunatech.webdata.cuisine.model

import com.lunatech.webdata.cuisine.mllib.ClassMetrics

/**
 *
 */

case class PredictedRecipe(id: Int, ingredients: Seq[String], predictions: Seq[PredictionData])

case class PredictionData(model: String, prediction: String, metrics: ClassMetrics)
