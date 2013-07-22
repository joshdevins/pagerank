package net.joshdevins.pagerank.cassovary.graph.node

import com.twitter.cassovary.graph.{Node, GraphDir, StoredGraphDir}
import com.twitter.cassovary.graph.GraphDir._
import com.twitter.cassovary.graph.StoredGraphDir._

sealed abstract class WeightedNode private[graph] (val id: Int, weights: Array[Double]) extends Node {
  def edges(dir: GraphDir): Seq[(Int, Double)] = dir match {
    case GraphDir.OutDir => outboundNodes.zip(weights)
    case GraphDir.InDir => inboundNodes.zip(weights)
  }
}

final object WeightedNode {

  /** Creates a [[WeightedNode]] with no out-edges.
    */
  def apply(id: Int, dir: StoredGraphDir): WeightedNode =
    apply(id, Array.empty[Int], Array.empty[Double], dir)

  /** Creates a [[WeightedNode]] with out-edges that have weights.
    */
  def apply(id: Int, neighbors: Array[Int], weights: Array[Double], dir: StoredGraphDir): WeightedNode = {
    require(neighbors.size == weights.size, "There should be an equal number of neighbors and weights")

    dir match {
      case StoredGraphDir.OnlyIn =>
        new WeightedNode(id, weights) {
          def inboundNodes = neighbors
          def outboundNodes = Nil
        }
      case StoredGraphDir.OnlyOut =>
        new WeightedNode(id, weights) {
          def inboundNodes = Nil
          def outboundNodes = neighbors
        }
      case StoredGraphDir.Mutual =>
        new WeightedNode(id, weights) {
          def inboundNodes = neighbors
          def outboundNodes = neighbors
        }
    }
  }
}
