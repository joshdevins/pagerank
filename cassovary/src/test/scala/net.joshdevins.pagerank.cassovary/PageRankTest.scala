package net.joshdevins.pagerank.cassovary

import com.twitter.cassovary.graph.{DirectedGraph, TestGraphs}
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

class PageRankTest extends FunSuite with ShouldMatchers {

  val tolerance = math.pow(1.0, -20)
  val graphG6: DirectedGraph = TestGraphs.g6
  val graphComplete: DirectedGraph = TestGraphs.generateCompleteGraph(10)

  test("returns a uniform array after 0 iterations") {
    val params = PageRankParams(0.9, 0)
    val actual = PageRank(graphG6, params)

    val expected = Array.fill[Double](16)(0)
    (10 to 15).foreach { i => expected(i) = 1.0 / 6 }

    expected.zipWithIndex.foreach { case (e, i) =>
      actual(i) should be (e plusOrMinus tolerance)
    }
  }

  test("returns correct values after 1 iteration") {
    val params = PageRankParams(0.1, 1)
    val actual = PageRank(graphG6, params)

    (0 until 10).foreach { i => expect(0.0)(actual(i)) }

    actual(10) should be (0.1 / 6 + 0.9 / 12 plusOrMinus tolerance)
    actual(11) should be (0.1 / 6 + 0.9 * (1.0 / 18 + 1.0 / 12) plusOrMinus tolerance)
    actual(12) should be (0.1 / 6 + 0.9 * (1.0 / 6 + 1.0 / 18) plusOrMinus tolerance)
    actual(13) should be (0.1 / 6 + 0.1 / 2 plusOrMinus tolerance)
    actual(14) should be (0.1 / 6 + 0.9 / 3 plusOrMinus tolerance)
    actual(15) should be (1.0 / 6 plusOrMinus tolerance)
  }

  test("values sum to 1 after 2 iterations") {
    val params = PageRankParams(0.1, 2)
    val actual = PageRank(graphG6, params)

    expect(1.0)(actual.sum)
  }

  test("for a complete graph, after 100 iterations, initial values are maintained") {
    val params = PageRankParams(0.1, 100)
    val actual = PageRank(graphComplete, params)

    graphComplete.foreach { n => expect(0.1)(actual(n.id)) }
  }

}
