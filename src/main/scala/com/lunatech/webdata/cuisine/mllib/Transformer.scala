package com.lunatech.webdata.cuisine.mllib

/**
 * Interface for transforming flow data (e.g. filtering, sorting,
 * applying smart algorithms on it...)
 */
trait Transformer {

  def transform(flowData: FlowData): FlowData

}
