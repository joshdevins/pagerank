package net.joshdevins.pagerank.cassovary

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

final class WeightedNodeTest
  extends FunSuite
  with ShouldMatchers {

  test("basic properties") {
    val node = buildWeightedNode(100, 3)

    node.inboundNodes should be (Nil)
    node.outboundNodes should be (Seq(0, 1, 2))
    node.edges should be (Seq((0, 0d), (1, 1d), (2, 2d)))
  }

  test("create dangling node - no out edges") {
    val node = WeightedNode(100)

    node.inboundNodes should be (Nil)
    node.outboundNodes should be ('empty)
    node.edges should be ('empty)
  }

  test("attempt to create node with missing weights") {
    intercept[IllegalArgumentException] {
      WeightedNode(100, Array(1), Array.empty[Double])
    }
  }

  test("equality") {
    val left = buildWeightedNode(1, 100)
    val right = buildWeightedNode(1, 100)

    left should be (right)
  }

  test("order") {
    val left = buildWeightedNode(1, 100)
    val right = buildWeightedNode(2, 100)

    left.compare(right) should be (-1)
    right.compare(left) should be (1)
    left.compare(left) should be (0)
  }

  private def buildWeightedNode(id: Int, numOutEdges: Int): WeightedNode = {
    val neighbors = (0 until numOutEdges).toArray
    val weights = neighbors.map(_.toDouble)

    WeightedNode(id, neighbors, weights)
  }
}
