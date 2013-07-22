package net.joshdevins.pagerank.cassovary

import com.twitter.cassovary.graph._
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

class PageRankTest extends FunSuite with ShouldMatchers {

  val tolerance = math.pow(1.0, -20)

  val graphWithoutDangle = ArrayBasedDirectedGraph(() => Seq(
      NodeIdEdgesMaxId(0, Array(4)),
      NodeIdEdgesMaxId(1, Array(0)),
      NodeIdEdgesMaxId(2, Array(0)),
      NodeIdEdgesMaxId(3, Array(1, 2)),
      NodeIdEdgesMaxId(4, Array(2, 3))
    ).iterator, StoredGraphDir.BothInOut)

  val graphWithDangle = ArrayBasedDirectedGraph(() => Seq(
      NodeIdEdgesMaxId(0, Array()), // dangling
      NodeIdEdgesMaxId(1, Array(0)),
      NodeIdEdgesMaxId(2, Array(0)),
      NodeIdEdgesMaxId(3, Array(1, 2)),
      NodeIdEdgesMaxId(4, Array(2, 3))
    ).iterator, StoredGraphDir.BothInOut)

  test("returns a uniform array after 0 iterations") {
    val params = PageRankParams(0.9, 0)
    val actual = PageRank(graphWithoutDangle, params)

    val expected = Array.fill[Double](5)(1.0 / 5)

    expected.zipWithIndex.foreach { case (e, i) =>
      actual(i) should be (e plusOrMinus tolerance)
    }
  }

  test("returns correct values after 1 iteration") {
    val params = PageRankParams(0.1, 1)
    val actual = PageRank(graphWithoutDangle, params)

    // verified against Octave/Matlab implementation
    // TODO: show work by hand
    actual(0) should be (0.37    plusOrMinus tolerance)
    actual(1) should be (0.14333 plusOrMinus tolerance)
    actual(2) should be (0.14333 plusOrMinus tolerance)
    actual(3) should be (0.14333 plusOrMinus tolerance)
    actual(4) should be (0.2     plusOrMinus tolerance)
  }

  test("values sum to 1 after 2 iterations") {
    val params = PageRankParams(0.1, 2)
    val actual = PageRank(graphWithoutDangle, params)

    actual.sum should be (1.0 plusOrMinus tolerance)
  }

  test("for a complete graph, after 100 iterations, initial values are maintained") {
    val graphComplete = TestGraphs.generateCompleteGraph(10)
    val params = PageRankParams(0.1, 100)
    val actual = PageRank(graphComplete, params)

    graphComplete.foreach { n => expect(0.1)(actual(n.id)) }
  }

}
