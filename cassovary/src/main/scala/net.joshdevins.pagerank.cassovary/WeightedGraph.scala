package net.joshdevins.pagerank.cassovary

import com.twitter.cassovary.graph.{Graph, Node}

/** This class is an implementation of the directed graph trait that is backed
  * by an iterable. Note that the iterable should be dense, containing no empty
  * values or nulls.
  */
final class WeightedGraph(
    nodes: Iterable[WeightedNode],
    val numNodes: Int,
    val numEdges: Long,
    val maxNodeId: Int)
  extends Graph
  with Iterable[WeightedNode] {

  override def iterator: Iterator[WeightedNode] = nodes.iterator

  /** Note that this is highly inefficient and should be avoided.
    */
  override def getNodeById(id: Int): Option[WeightedNode] = {
    nodes.find { node =>
      node.id == id
    }
  }
}

final object WeightedGraph {

  /** Note that this is a very inefficient means to calculate statistics but is
    * effective for small datasets like tests.
    */
  def apply(nodes: Seq[WeightedNode]): WeightedGraph = {
    val nodeIds =
      nodes.
        filter(_ != null).
        flatMap { node =>
          Seq(node.id) ++ node.neighbors
        }.
        distinct

    var numEdges = 0l
    nodes.
      filter(_ != null).
      foreach(numEdges += _.neighbors.size)

    new WeightedGraph(nodes, nodes.size, numEdges, nodeIds.max)
  }
}
