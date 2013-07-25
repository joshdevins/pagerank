package net.joshdevins.pagerank.cassovary

import java.util.concurrent.Executors

import com.twitter.cassovary.util.io.AdjacencyListGraphReader

final object PageRankRunner extends App {

  println("\nLoading graph")
  var startTime = System.currentTimeMillis

  val graph = WeightedGraphReader(args(0), Some("part-"))

  println("Graph loaded:")
  printf("\ttook: %s\n", System.currentTimeMillis - startTime)
  printf("\tnodes: %s\n", graph.nodeCount)
  printf("\tdirected edges: %s\n", graph.edgeCount)

  // took: 53859
  // nodes: 14,773,606
  // directed edges: 257,012,280

  println("Running PageRank")
  startTime = System.currentTimeMillis

  val results = PageRank(graph, new PageRankParams(0.15, 30))

  println("PageRank run:")
  printf("\ttook: %s\n", System.currentTimeMillis - startTime)
}
