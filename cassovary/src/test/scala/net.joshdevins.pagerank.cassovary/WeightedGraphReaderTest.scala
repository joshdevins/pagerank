package net.joshdevins.pagerank.cassovary

import java.io.File

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

import WeightedGraphReader._

final class WeightedGraphReaderTest
  extends FunSuite
  with ShouldMatchers {

  val nodesInParts = Map(
    // 0 is a dangling node and should be added after the fact
    1 -> Seq(
      WeightedNode(1, Array(0), Array(0.5 / 0.5))),
    2 -> Seq(
      WeightedNode(2, Array(0), Array(0.5 / 0.5))),
    3 -> Seq(
      WeightedNode(3, Array(1, 2), Array(1.0 / 1.5, 0.5 / 1.5)),
      WeightedNode(4, Array(2, 3), Array(0.5 / 1.5, 1.0 / 1.5)))
  )

  val nodes = nodesInParts.values.flatten.toSeq

  test("read part files separately") {
    val partial1 = readPartFile(new File("src/test/resources/graph/part-00001"))
    partial1.nodes should be (nodesInParts(1))
    partial1.maxId should be (1)
    partial1.numEdges should be (1)

    val partial2 = readPartFile(new File("src/test/resources/graph/part-00002"))
    partial2.nodes should be (nodesInParts(2))
    partial2.maxId should be (2)
    partial2.numEdges should be (1)

    val partial3 = readPartFile(new File("src/test/resources/graph/part-00003"))
    partial3.nodes should be (nodesInParts(3))
    partial3.maxId should be (4)
    partial3.numEdges should be (4)
  }

  test("convert sequence of nodes to indexed nodes") {
    val indexedNodes = toIndexedNodes(nodes, 4)

    indexedNodes.size should be (5)
    indexedNodes(0) should be (null)
    indexedNodes(1).id should be (1)
    indexedNodes(4).id should be (4)
    indexedNodes.filter(_ != null) should be (nodes.toArray)
  }

  test("add dangling nodes") {
    val allNodes = addDanglingNodes(nodes)
    val danglingNodes = Seq(WeightedNode(0))

    allNodes.size should be (nodes.size + 1)
    allNodes should be (nodes ++ danglingNodes)
  }

  test("read only part files in a directory into a graph") {
    // should ignore the file called "ignore" which has actual edges in it
    val graph = WeightedGraphReader("src/test/resources/graph", Some("part-"))

    graph.nodeCount should be (5)
    graph.edgeCount should be (6)
    graph.iterator.toSeq.sorted should be (addDanglingNodes(nodes).toSeq.sorted)
  }

  test("read all files in a directory") {
    val graph = WeightedGraphReader("src/test/resources/graph")

    graph.nodeCount should be (6)
    graph.edgeCount should be (7)

    val newNodes = nodes ++ Seq(WeightedNode(100, Array(100), Array(1.0)))
    graph.iterator.toSeq.sorted should be (addDanglingNodes(newNodes).toSeq.sorted)
  }
}
