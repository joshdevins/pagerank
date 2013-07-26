package net.joshdevins.pagerank.cassovary

import scala.collection._

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

final class WeightedGraphTest
  extends FunSuite
  with ShouldMatchers {

  val nodes = Seq(
    WeightedNode(0, Array(1, 2), Array(1d, 1d)),
    WeightedNode(1, Array(0),    Array(1d)),
    WeightedNode(2, Array(1),    Array(1d))
  )

  test("basic properties") {
    val graph = new WeightedGraph(nodes, 3, 4, 2)

    graph.numNodes should be (3)
    graph.numEdges should be (4)
    graph.maxNodeId should be (2)

    graph.iterator.toSeq should be (nodes)

    graph.getNodeById(0) should be (Some(nodes(0)))
    graph.getNodeById(1) should be (Some(nodes(1)))
    graph.getNodeById(2) should be (Some(nodes(2)))
    graph.getNodeById(3) should be (None)

    graph.getNodeById(-1) should be (None)
    graph.getNodeById(99) should be (None)
  }

  test("builder function") {
    val graph = WeightedGraph(nodes)

    graph.numNodes should be (3)
    graph.numEdges should be (4)
    graph.maxNodeId should be (2)
  }
}
