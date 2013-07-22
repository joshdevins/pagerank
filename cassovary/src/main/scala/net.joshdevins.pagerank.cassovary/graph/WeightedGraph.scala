package net.joshdevins.pagerank.cassovary.graph

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{ExecutorService, Future}

import scala.collection.mutable

import com.google.common.annotations.VisibleForTesting
import com.google.common.util.concurrent.MoreExecutors
import com.twitter.cassovary.graph.{Node, DirectedGraph, StoredGraphDir}
import com.twitter.cassovary.graph.StoredGraphDir._
import com.twitter.cassovary.graph.node._
import com.twitter.cassovary.util.ExecutorUtils
import com.twitter.ostrich.stats.Stats
import net.lag.logging.Logger

import net.joshdevins.pagerank.cassovary.graph.node.WeightedNode

/** This case class holds a node's id, all its out edges, weights, and the max
  * id of itself and ids of nodes in its out edges
  */
case class NodeIdEdgesWeightsMaxId(var id: Int, var edges: Array[Int], var weights: Array[Double], var maxId: Int)

object NodeIdEdgesWeightsMaxId {
  def apply(id: Int, edges: Array[Int], weights: Array[Double]) =
      new NodeIdEdgesWeightsMaxId(id, edges, weights, edges.foldLeft[Int](id)((x, y) => x max y))
}

/** This case class holds a part of the total graph loaded in one thread
  * it consists of a seq of nodes with out-edges, a max overall id, and
  * a max id of nodes with out-edges
  */
private case class NodesMaxIds(nodesInOneThread: Seq[WeightedNode], maxIdInPart: Int, nodeWithOutEdgesMaxIdInPart: Int)

/** Provides methods for constructing an array based, weighted graph
  */
object WeightedGraph {
  private lazy val log = Logger.get

  /**
   * Construct an array-based graph from an sequence of iterators over NodeIdEdgesWeightsMaxId
   * This function builds the array-based graph from a seq of nodes with out edges
   * using the following steps:
   * 0. read from file and construct a sequence of Nodes
   * 1. create an array of size maxNodeid
   * 2. mark all positions in the array where there is a node
   * 3. instantiate nodes that only have in-edges (thus has not been created in the input)
   * next steps apply only if in-edge is needed
   * 4. calculate in-edge array sizes
   * 5. instantiate in-edge arrays
   * 6. iterate over the sequence of nodes again, instantiate in-edges
   */
  def apply(
    iteratorSeq: Seq[() => Iterator[NodeIdEdgesWeightsMaxId]],
    executorService: ExecutorService): WeightedGraph = {

    log.debug("loading nodes and out edges from file in parallel")
    val futures = Stats.time("graph_dump_load_partial_nodes_and_out_edges_parallel") {
      def readOutEdges(iteratorFunc: () => Iterator[NodeIdEdgesWeightsMaxId]): NodesMaxIds = {
        Stats.time("graph_load_read_out_edge_from_dump_files") {
          val nodes = new mutable.ArrayBuffer[WeightedNode]
          var newMaxId = 0
          var varNodeWithOutEdgesMaxId = 0
          var edges = Array.empty[Int]
          var weights = Array.empty[Double]

          iteratorFunc().foreach { item =>
            newMaxId = newMaxId.max(item.maxId)
            varNodeWithOutEdgesMaxId = varNodeWithOutEdgesMaxId.max(item.id)
            val newNode = WeightedNode(item.id, item.edges, item.weights)
            nodes += newNode
          }
          NodesMaxIds(nodes, newMaxId, varNodeWithOutEdgesMaxId)
        }
      }

      ExecutorUtils.
        parallelWork[() => Iterator[NodeIdEdgesWeightsMaxId], NodesMaxIds](
          executorService,
          iteratorSeq,
          readOutEdges)
    }

    val nodesOutEdges = new mutable.ArrayBuffer[Seq[WeightedNode]]
    var maxNodeId = 0
    var nodeWithOutEdgesMaxId = 0

    futures.toArray.map { future =>
      val f = future.asInstanceOf[Future[NodesMaxIds]]
      val NodesMaxIds(nodesInOneThread, maxIdInPart, nodeWithOutEdgesMaxIdInPart) = f.get

      nodesOutEdges += nodesInOneThread
      maxNodeId = maxNodeId.max(maxIdInPart)
      nodeWithOutEdgesMaxId = nodeWithOutEdgesMaxId.max(nodeWithOutEdgesMaxIdInPart)
    }

    val nodeIdSet = new Array[Byte](maxNodeId + 1)
    val table = new Array[WeightedNode](maxNodeId + 1)

    log.debug("mark the ids of all stored nodes in nodeIdSet")
    Stats.time("graph_load_mark_ids_of_stored_nodes") {
      def markAllNodes = (nodes: Seq[WeightedNode]) => {
        nodes.foreach { node =>
          table(node.id) = node
          nodeIdSet(node.id) = 1
          node.outboundNodes.foreach { outEdge => nodeIdSet(outEdge) = 1 }
        }
      }

      ExecutorUtils.parallelWork[Seq[WeightedNode], Unit](executorService, nodesOutEdges, markAllNodes)
    }

    // creating nodes that have only in edges but no out edges
    // also calculates the total number of edges
    val nodesWithNoOutEdges = new mutable.ArrayBuffer[Node]
    var nodeWithOutEdgesCount = 0
    var numNodes = 0
    var numEdges = 0l

    log.debug("creating nodes that have only in-coming edges")
    Stats.time("graph_load_creating_nodes_without_out_edges") {
      (0 to maxNodeId).foreach { id =>
        if (nodeIdSet(id) == 1) {
          numNodes += 1
          if (table(id) == null) {
            table(id) = WeightedNode(id)
          } else {
            nodeWithOutEdgesCount += 1
            numEdges += table(id).outboundNodes.size
          }
        }
      }
    }

    new WeightedGraph(
      table,
      maxNodeId,
      numNodes,
      numEdges)
  }

  @VisibleForTesting
  def apply(
    iteratorFunc: () => Iterator[NodeIdEdgesWeightsMaxId]): WeightedGraph =
    apply(Seq(iteratorFunc), MoreExecutors.sameThreadExecutor())
}


/**
 * This class is an implementation of the directed graph trait that is backed by an array
 * The private constructor takes as its input a list of (@see Node) nodes, then stores
 * nodes in an array. It also builds all edges which are also stored in array.
 *
 * @param nodes the list of nodes with edges instantiated
 * @param _maxNodeId the max node id in the graph
 * @param nodeCount the number of nodes in the graph
 * @param edgeCount the number of edges in the graph
 */
final class WeightedGraph private (
    nodes: Array[WeightedNode],
    _maxNodeId: Int,
    override val nodeCount: Int,
    override val edgeCount: Long)
  extends DirectedGraph
  with Iterable[WeightedNode] {

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
