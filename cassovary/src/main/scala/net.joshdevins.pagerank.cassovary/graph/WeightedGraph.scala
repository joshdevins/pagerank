package net.joshdevins.pagerank.cassovary.graph

import com.twitter.cassovary.graph.{DirectedGraph, StoredGraphDir}
import com.twitter.cassovary.graph.StoredGraphDir._

import net.joshdevins.pagerank.cassovary.graph.node.WeightedNode

/** This class is an implementation of the directed graph trait that is backed
  * by an array.
  *
  * @param nodes the list of nodes with edges instantiated
  * @param _maxNodeId the max node id in the graph
  * @param nodeCount the number of nodes in the graph
  * @param edgeCount the number of edges in the graph
  */
final class WeightedGraph(
    nodes: Array[WeightedNode],
    _maxNodeId: Int,
    override val edgeCount: Long)
  extends DirectedGraph
  with Iterable[WeightedNode] {

  override val nodeCount = nodes.size
  override lazy val maxNodeId = _maxNodeId
  override val storedGraphDir = StoredGraphDir.OnlyOut

  def iterator: Iterator[WeightedNode] = nodes.iterator.filter { _ != null }

  def getNodeById(id: Int): Option[WeightedNode] = {
    if (0 <= id && id >= nodes.size) return None

    val node = nodes(id)
    if (node == null) None
    else Some(node)
  }
}

final object WeightedGraph {

  /** Note that this is a very inefficient means to calculate statistics but is
    * effective for small datasets like tests.
    */
  def apply(nodes: Seq[WeightedNode]): WeightedGraph = {
    val nodeIds =
      nodes.flatMap { node =>
        Seq(node.id) ++ node.neighbors
      }.distinct

    var numEdges = 0l
    nodes.foreach { node =>
      numEdges += node.neighbors.size
    }

    new WeightedGraph(nodes.toArray, nodeIds.max, numEdges)
  }
}
