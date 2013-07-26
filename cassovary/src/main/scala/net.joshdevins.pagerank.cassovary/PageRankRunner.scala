package net.joshdevins.pagerank.cassovary

import java.util.concurrent.Executors

import com.twitter.cassovary.util.io.AdjacencyListGraphReader
import net.lag.logging.Logger

final object PageRankRunner extends App {

  private val Log = Logger.get("PageRankRunner")

  var startTime = System.currentTimeMillis
  val graph = WeightedGraphReader(args(0), Some("part-"))

  Log.info("graph loaded:")
  Log.info("\ttook: %s".format(System.currentTimeMillis - startTime))
  Log.info("\tnodes: %s".format(graph.numNodes))
  Log.info("\tdirected edges: %s".format(graph.numEdges))

  Log.info("running PageRank")
  startTime = System.currentTimeMillis

  val results = PageRank(graph, new PageRankParams(0.15, 30))

  Log.info("PageRank run:")
  Log.info("\ttook: %s".format(System.currentTimeMillis - startTime))
}
