package net.joshdevins.pagerank.cassovary

import com.twitter.cassovary.graph.{DirectedGraph, StoredGraphDir, TestGraphs}
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

class PageRankTest
  extends FunSuite
  with ShouldMatchers {

  val tolerance = 1.0E-10

  val graphWithoutDangle = WeightedGraph(Array(
      WeightedNode(0, Array(4),    Array(1.0)),
      WeightedNode(1, Array(0),    Array(1.0)),
      WeightedNode(2, Array(0),    Array(1.0)),
      WeightedNode(3, Array(1, 2), Array(1.0 / 1.5, 0.5 / 1.5)),
      WeightedNode(4, Array(2, 3), Array(0.5 / 1.5, 1.0 / 1.5))
    ))

  val graphWithDangle = WeightedGraph(Array(
      WeightedNode(0, Array(),     Array()),    // dangling
      WeightedNode(1, Array(0),    Array(1.0)),
      WeightedNode(2, Array(0),    Array(1.0)),
      WeightedNode(3, Array(1, 2), Array(1.0 / 1.5, 0.5 / 1.5)),
      WeightedNode(4, Array(2, 3), Array(0.5 / 1.5, 1.0 / 1.5))
    ))

  test("returns a uniform array after 0 iterations") {
    val params = PageRankParams(0.9, 0)
    val actual = PageRank(graphWithoutDangle, params)

    val expected = Array.fill[Double](5)(1.0 / 5)

    expected.zipWithIndex.foreach { case (e, i) =>
      actual(i) should be (e plusOrMinus tolerance)
    }
  }

  test("returns correct values after 1 iteration on graph without dangling nodes") {
    val params = PageRankParams(0.1, 1)
    val actual = PageRank(graphWithoutDangle, params)

    // verified against Octave/Matlab implementation
    // TODO: show work by hand
    println("tolerance: " + tolerance)
    actual(0) should be (0.38 plusOrMinus tolerance)
    actual(1) should be (0.14 plusOrMinus tolerance)
    actual(2) should be (0.14 plusOrMinus tolerance)
    actual(3) should be (0.14 plusOrMinus tolerance)
    actual(4) should be (0.20 plusOrMinus tolerance)
  }

  test("returns correct values after 1 iteration on graph with dangling nodes") {
    val params = PageRankParams(0.1, 1)
    val actual = PageRank(graphWithDangle, params)

    // verified against Octave/Matlab implementation
    // TODO: show work by hand
    actual(0) should be (0.380 plusOrMinus tolerance)
    actual(1) should be (0.185 plusOrMinus tolerance)
    actual(2) should be (0.185 plusOrMinus tolerance)
    actual(3) should be (0.185 plusOrMinus tolerance)
    actual(4) should be (0.065 plusOrMinus tolerance)
  }

  test("values sum to 1 after n iterations") {
    def sumsToOne(graph: WeightedGraph, numIterations: Int): Unit = {
      val params = PageRankParams(0.1, numIterations)
      val actual = PageRank(graph, params)

      actual.sum should be (1.0 plusOrMinus tolerance)
    }

    sumsToOne(graphWithoutDangle, 1)
    sumsToOne(graphWithoutDangle, 2)
    sumsToOne(graphWithoutDangle, 100)

    sumsToOne(graphWithDangle, 1)
    sumsToOne(graphWithDangle, 2)
    sumsToOne(graphWithDangle, 100)
  }

  test("for a complete graph, after 100 iterations, initial values are maintained") {
    // a complete graph is where each node follows every other node
    def generateCompleteGraph(numNodes: Int) = {
      val allNodes = (1 to numNodes)
      val testNodes = (1 to numNodes).toList.map { source =>
        val allBut = allNodes.filter(_ != source)
        WeightedNode(source, allBut.toArray, Array.fill[Double](numNodes - 1)(1.0 / (numNodes - 1)))
      }

      WeightedGraph(testNodes)
    }

    val graphComplete = generateCompleteGraph(10)
    val params = PageRankParams(0.1, 100)
    val actual = PageRank(graphComplete, params)

    graphComplete.foreach { node =>
      expect(0.1)(actual(node.id))
    }
  }

}
