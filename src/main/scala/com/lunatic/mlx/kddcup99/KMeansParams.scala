package com.lunatic.mlx.kddcup99

/**
  * Small configuration for the KMeans algorithm
  *
  * @param k
  * @param epsilon
  * @param maxIterations
  * @param runs
  * @param norm @see ImportData
  */
case class KMeansParams(k: Int, epsilon: Double, maxIterations: Int, runs: Int, norm: String)
