package net.joshdevins.pagerank.cassovary

import com.twitter.cassovary.graph.StoredGraphDir
import com.twitter.cassovary.graph.StoredGraphDir._
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

final class WeightedGraphTest
  extends FunSuite
  with ShouldMatchers {

  val nodes = Array(
    WeightedNode(0, Array(1, 2), Array(1d, 1d)),
    WeightedNode(1, Array(0),    Array(1d)),
    WeightedNode(2, Array(1),    Array(1d)),
    null
  )

  test("basic properties") {
    val graph = new WeightedGraph(nodes, 2, 3, 4)

    graph.maxNodeId should be (2)
    graph.nodeCount should be (3)
    graph.edgeCount should be (4)

    graph.storedGraphDir should be (StoredGraphDir.OnlyOut)
    graph.iterator.toArray should be (nodes.dropRight(1)) // remove null entry first

    graph.getNodeById(0) should be (Some(nodes(0)))
    graph.getNodeById(1) should be (Some(nodes(1)))
    graph.getNodeById(2) should be (Some(nodes(2)))
    graph.getNodeById(3) should be (None)

    graph.getNodeById(-1) should be (None)
    graph.getNodeById(99) should be (None)
  }

  test("builder function") {
    val graph = WeightedGraph(nodes)

    graph.maxNodeId should be (2)
    graph.nodeCount should be (3)
    graph.edgeCount should be (4)
  }
}
