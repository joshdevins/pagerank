package net.joshdevins.pagerank.cassovary

import com.twitter.cassovary.graph.{DirectedGraph, GraphDir, Node}
import com.twitter.cassovary.util.Progress
import net.lag.logging.Logger

import PageRank._

final class PageRank(graph: DirectedGraph, params: PageRankParams) {

  private val log = Logger.get("PageRank")

  val perNodeAlpha = params.alpha / graph.nodeCount // assumes a uniform prior
  val numDanglingNodes = {
    // count number of dangling nodes in the graph (no out edges)
    var count = 0
    graph.foreach { node =>
      if (isDanglingNode(node))
        count += 1
    }
    count
  }
  log.info("number of nodes: " + graph.nodeCount)
  log.info("number of dangling nodes: " + numDanglingNodes)

  def run: Array[Double] = {

    // let the user know if they can save memory
    if (graph.maxNodeId.toDouble / graph.nodeCount > 1.1 && graph.maxNodeId - graph.nodeCount > 1000000)
      log.info("Warning - you may be able to reduce the memory usage of PageRank by renumbering this graph!")

    var values = new Array[Double](graph.maxNodeId + 1)

    // initialize PageRank to some "random" values
    log.info("Initializing PageRank run")
    val progress = buildProgress("init")
    val initialPageRankValue = 1.0d / graph.nodeCount

    graph.foreach { node =>
      values(node.id) = initialPageRankValue
      progress.inc
    }

    // begin iterating until n (no delta checking yet)
    (0 until params.numIterations).foreach { i =>
      log.info("iteration %s".format(i))
      values = iterate(values)
    }

    values
  }

  /** Memory cost: creates a new array for every iteration (easily garbage collectable)
    * Computation cost:
    *  loops through all nodes twice (propogate/calculate PageRank, apply teleport probability)
    *  loops through each neighbour node of every node, every iteration
    */
  def iterate(beforeIterationValues: Array[Double]): Array[Double] = {

    // values after this iteration completes
    val afterIterationValues = new Array[Double](graph.maxNodeId + 1)

    log.info("Calculate PageRank on nodes")
    val calcProgress = buildProgress("iter_calc")
    graph.foreach { node =>
      val givenPageRank = beforeIterationValues(node.id) / node.neighborCount(GraphDir.OutDir)
      node.neighborIds(GraphDir.OutDir).foreach { neighborId =>
        afterIterationValues(neighborId) += givenPageRank
      }

      calcProgress.inc
    }

    log.info("Apply teleport probability")
    val teleportProgress = buildProgress("iter_teleport")
    if (params.alpha > 0.0) {
      graph.foreach { node =>
        afterIterationValues(node.id) = perNodeAlpha + ((1.0 - params.alpha) * afterIterationValues(node.id))

        teleportProgress.inc
      }
    }

    log.info("Distribute dangling node mass")
    val danglingProgress = buildProgress("iter_dangle")
    if (numDanglingNodes > 0) {
      val remainingMass = 1.0 - afterIterationValues.sum
      val perNodeRemainingMass = remainingMass / (graph.nodeCount - numDanglingNodes)
      graph.foreach { node =>
        if (isDanglingNode(node))
          afterIterationValues(node.id) += perNodeRemainingMass
      }
    }

    afterIterationValues
  }

  private def isDanglingNode(node: Node): Boolean =
    node.neighborCount(GraphDir.OutDir) == 0

  private def buildProgress(name: String): Progress =
    Progress("pagerank_" + name, math.pow(2, 16).toInt, Some(graph.nodeCount))
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
    * @param graph A {@link DirectedGraph} instance
    * @param params {@link PageRankParams}
    *
    * @return An array of doubles, with indices corresponding to node ids
    */
  def apply(graph: DirectedGraph, params: PageRankParams): Array[Double] = {
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
  alpha: Double = 0.85,
  numIterations: Int = 10)
