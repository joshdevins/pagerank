package net.joshdevins.pagerank.cassovary

import com.twitter.cassovary.graph.{DirectedGraph, GraphDir, Node}
import com.twitter.cassovary.util.Progress
import net.lag.logging.Logger

import PageRank._

final class PageRank(graph: WeightedGraph, params: PageRankParams) {

  private val log = Logger.get("PageRank")

  val perNodeAlpha = params.alpha / graph.numNodes // assumes a uniform prior
  val numDanglingNodes = {
    // count number of dangling nodes in the graph (no out edges)
    var count = 0
    graph.foreach { node =>
      if (isDanglingNode(node))
        count += 1
    }
    count
  }
  log.info("number of nodes: " + graph.numNodes)
  log.info("number of dangling nodes: " + numDanglingNodes)

  def run: Array[Double] = {
    var values = new Array[Double](graph.maxNodeId + 1)

    // initialize PageRank to some "random" values
    log.info("initializing PageRank run")
    val initialPageRankValue = 1.0d / graph.numNodes

    graph.foreach { node =>
      values(node.id) = initialPageRankValue
    }

    // begin iterating until n (no delta checking yet)
    (1 to params.numIterations).foreach { i =>
      log.info("iteration %s".format(i))
      values = iterate(values)
    }

    values
  }

  /** Memory cost: creates a new array for every iteration (easily garbage collectable)
    * Computation cost:
    *  loops through all nodes three times (propogate/calculate PageRank, apply teleport probability, distribute dangling node mass)
    *  loops through each neighbour node of every node, every iteration
    */
  def iterate(beforeIterationValues: Array[Double]): Array[Double] = {

    // values after this iteration completes
    val afterIterationValues = new Array[Double](graph.maxNodeId + 1)

    log.info("  distribute node mass")
    graph.foreach { node =>
      node.foreachEdge { (id, weight) =>
        afterIterationValues(id) += beforeIterationValues(node.id) * weight // assumes weights are already normalized
      }
    }

    log.info("  apply teleport probability")
    if (params.alpha > 0.0) {
      graph.foreach { node =>
        afterIterationValues(node.id) = perNodeAlpha + ((1.0 - params.alpha) * afterIterationValues(node.id))
      }
    }

    log.info("  distribute dangling node mass")
    if (numDanglingNodes > 0) {
      val remainingMass = 1.0 - afterIterationValues.sum
      val perNodeRemainingMass = remainingMass / (graph.numNodes - numDanglingNodes)
      graph.foreach { node =>
        if (!isDanglingNode(node))
          afterIterationValues(node.id) += perNodeRemainingMass
      }
    }

    afterIterationValues
  }

  private def isDanglingNode(node: Node): Boolean =
    node.neighborCount(GraphDir.OutDir) == 0
}

/** A naive PageRank implementation. Not optimized and runs in a single thread.
  */
final object PageRank {

  /** Execute PageRank.
    *
    * Note that the memory usage of this implementation is
    * proportional to the graph's maxId - you might want to re-number the
    * graph before running PageRank.
    *
    * @param graph A [[WeightedGraph]] instance
    * @param params [[PageRankParams]]
    *
    * @return An array of doubles, with indices corresponding to node ids
    */
  def apply(graph: WeightedGraph, params: PageRankParams): Array[Double] = {
    new PageRank(graph, params).run
  }

  /** Execute a single iteration of PageRank, given the previous PageRank array
    *
    * @return The updated array
    */
  // def iterate(graph: DirectedGraph, params: PageRankParams, prArray: Array[Double]) = {
  //   new PageRank(graph, params).iterate(prArray: Array[Double])
  // }
}

/** Parameters for PageRank.
  *
  * @param alpha Probability of randomly jumping to another node, aka. telport probability
  * @param numIterations How many iterations do you want?
  */
final case class PageRankParams(
  alpha: Double = 0.15,
  numIterations: Int = 10)
