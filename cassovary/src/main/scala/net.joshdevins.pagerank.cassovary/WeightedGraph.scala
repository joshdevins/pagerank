package net.joshdevins.pagerank.cassovary

import com.twitter.cassovary.graph.{DirectedGraph, StoredGraphDir}
import com.twitter.cassovary.graph.StoredGraphDir._

/** This class is an implementation of the directed graph trait that is backed
  * by an array.
  *
  * @param nodes the list of nodes with edges instantiated
  * @param _maxNodeId the max node id in the graph
  * @param edgeCount the number of edges in the graph
  */
final class WeightedGraph(
    _nodes: Array[WeightedNode],
    _maxNodeId: Int,
    _nodeCount: Int,
    override val edgeCount: Long)
  extends DirectedGraph
  with Iterable[WeightedNode] {

  def nodes = _nodes // seems to prevent IllegalAccessExceptions...very strange
  override lazy val maxNodeId = _maxNodeId
  override val storedGraphDir = StoredGraphDir.OnlyOut
  override val nodeCount = _nodeCount

  override def iterator: Iterator[WeightedNode] = nodes.iterator.filter { _ != null }

  /** Provide more efficient means to iterate over the array and skip
    * nulls/gaps where no node exists.
    */
  override def foreach[A](fn: (WeightedNode) => A): Unit = {
    var i = 0
    val size = nodes.size
    while(i < size) {
      val node = nodes(i)
      if (node != null) fn(node)
      i += 1
    }
  }

  def getNodeById(id: Int): Option[WeightedNode] = {
    if (id < 0 || id >= nodes.size) return None

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

    new WeightedGraph(nodes.toArray, nodeIds.max, nodeIds.size, numEdges)
  }
}
