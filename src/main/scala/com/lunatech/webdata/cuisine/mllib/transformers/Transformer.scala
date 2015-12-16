package com.lunatech.webdata.cuisine.mllib.transformers

import com.lunatech.webdata.cuisine.mllib.FlowData

/**
 * Interface for transforming flow data (e.g. filtering, sorting,
 * applying smart algorithms on it...)
 */
trait Transformer[T] {

  def transform(input: T): FlowData

}
