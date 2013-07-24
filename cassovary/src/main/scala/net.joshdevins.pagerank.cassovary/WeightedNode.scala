package net.joshdevins.pagerank.cassovary

import com.twitter.cassovary.graph.{Node, GraphDir}
import com.twitter.cassovary.graph.GraphDir._

/** A simple node that supports holding neighbors nodes with edge weights. Note
  * that this only supports outbound edges for simplicity.
  */
final case class WeightedNode(id: Int, neighbors: Array[Int], weights: Array[Double])
  extends Node
  with Ordered[WeightedNode] {

  require(neighbors.size == weights.size, "There should be an equal number of neighbors and weights")

  override val inboundNodes: Seq[Int] = Nil
  override val outboundNodes: Seq[Int] = neighbors

  /** Returns the edges as a sequence of neighbor/node ID's and weight tuples.
    */
  def edges: Seq[(Int, Double)] = neighbors.zip(weights)

  /** Allow ordering by ID.
    */
  override def compare(other: WeightedNode): Int = this.id compare other.id

  /** Override equality to do deep equality on [[Array]] as [[Seq]].
    */
  override def equals(any: Any): Boolean = {
    if (super.equals(any)) return true
    val other = any.asInstanceOf[WeightedNode]

    id == other.id &&
    neighbors.toSeq == other.neighbors.toSeq &&
    weights.toSeq == other.weights.toSeq
  }

  override def toString: String = {
    def partialSeqToString(name: String, seq: Seq[Any]) = {
      "%s[%d]: %s".format(
        name,
        seq.size,
        seq.take(10).mkString(","))
    }

    "id: " + id + "; " +
    partialSeqToString("neighbors", neighbors) + "; " +
    partialSeqToString("weights", weights)
  }
}

final object WeightedNode {

  /** Creates a [[WeightedNode]] with no out-edges.
    */
  def apply(id: Int): WeightedNode =
    apply(id, Array.empty[Int], Array.empty[Double])
}
