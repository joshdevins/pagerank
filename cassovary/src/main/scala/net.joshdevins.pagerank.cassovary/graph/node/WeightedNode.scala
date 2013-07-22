package net.joshdevins.pagerank.cassovary.graph.node

import com.twitter.cassovary.graph.{Node, GraphDir}
import com.twitter.cassovary.graph.GraphDir._

final case class WeightedNode(val id: Int, neighbors: Array[Int], weights: Array[Double]) extends Node {

  require(neighbors.size == weights.size, "There should be an equal number of neighbors and weights")

  def inboundNodes = Nil
  def outboundNodes = neighbors

  def edges(dir: GraphDir): Seq[(Int, Double)] = dir match {
    case GraphDir.OutDir => outboundNodes.zip(weights)
    case GraphDir.InDir => inboundNodes.zip(weights)
  }
}

final object WeightedNode {

  /** Creates a [[WeightedNode]] with no out-edges.
    */
  def apply(id: Int): WeightedNode =
    apply(id, Array.empty[Int], Array.empty[Double])
}
