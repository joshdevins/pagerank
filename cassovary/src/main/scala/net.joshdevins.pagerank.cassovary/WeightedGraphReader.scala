package net.joshdevins.pagerank.cassovary

import java.io.File

import scala.collection._
import scala.io.Source

import net.lag.logging.Logger

final object WeightedGraphReader {

  private val Log = Logger.get("WeightedGraphReader")

  final case class PartialGraph(nodes: Seq[WeightedNode], numEdges: Long, maxId: Int)

  /** Reads in a multi-line adjacency list from multiple files in a directory.
    * Does not check for duplicate edges or nodes. If there are multiple file
    * parts in the directory, they will be read in parallel.
    *
    * You can optionally specify which files in a directory to read. For
    * example, you may have files starting with "part-" that you'd like to read.
    * Only these will be read in if you specify that as the file prefix.
    *
    * In each file, a node and its neighbors is defined by the first line being
    * that node's id and its number of neighbors, followed by that number of ids
    * on subsequent lines.
    *
    * For example (where `numOutEdges` is `2`):
    *
    * {{{
    * row numOutEdges
    * col0 val0
    * col1 val1
    * }}}
    *
    * @param directory the directory to read from
    * @param filenamePrefix the string that each part file starts with
    */
  def apply(directory: String, filenamePrefix: Option[String] = None): WeightedGraph = {
    val files = new File(directory).listFiles
    val filteredFiles =
      if (filenamePrefix.isDefined) files.filter(_.getName.startsWith(filenamePrefix.get))
      else files

    // get contents out of all files, in parallel, and merge results
    Log.info("reading graph from part files")
    val fullGraph =
      filteredFiles.par.
        map(readPartFile(_)).
        reduce { (left: PartialGraph, right: PartialGraph) =>
          PartialGraph(
            left.nodes ++ right.nodes,
            left.numEdges + right.numEdges,
            left.maxId max right.maxId)
        }

    Log.info("adding dangling nodes")
    val nodes = addDanglingNodes(fullGraph.nodes)

    new WeightedGraph(nodes, nodes.size, fullGraph.numEdges, fullGraph.maxId)
  }

  /** Given a sequence of [[WeightedNode]]s, determine the dangling nodes (those
    * with no out edges) and add them to the sequence. This always assumes that
    * the initial sequence does not already contain any dangling nodes, as will
    * usually be the case when processing sparse row-compressed adjacency lists.
    *
    * Note that this performs all actions on parallel collections.
    */
  def addDanglingNodes(nodes: Seq[WeightedNode]): Seq[WeightedNode] = {
    val existingNodeIds = nodes.par.map(_.id).toSet
    val danglingNodeIds = nodes.par.flatMap {
      _.neighbors.flatMap { neighbor =>
        if (!existingNodeIds.contains(neighbor)) Some(neighbor)
        else None
      }
    }.distinct
    val danglingNodes = danglingNodeIds.par.map(WeightedNode(_))

    nodes ++ danglingNodes
  }

  /** Reads a partial graph from a single file. This has a few properties to be
    * aware of:
    *
    *  - this is currently not robust to corrupt files in any way
    *  - this always assumes no blank lines and exactly the number of out edges
    *    that are said to be there by the row header
    *  - this assumes exactly one space as a separator and no padding
    */
  def readPartFile(file: File): PartialGraph = {
    val lines = Source.fromFile(file).getLines
    var numEdges = 0l
    var maxId = 0

    def conditionallyUpdateMaxId(id: Int): Unit =
      maxId = maxId.max(id)

    def split(line: String): (String, String) = {
      val spaceIndex = line.indexOf(" ")
      val first = line.substring(0, spaceIndex)
      val second = line.substring(spaceIndex + 1, line.length)

      (first, second)
    }

    def normalize(weights: Array[Double]): Array[Double] = {
      val sum = weights.sum
      weights.map { v =>
        v / sum
      }
    }

    def readNextNode: WeightedNode = {
      val (rowStr, numOutEdgesStr) = split(lines.next)
      val row = rowStr.toInt
      val numOutEdges = numOutEdgesStr.toInt

      numEdges += numOutEdges
      conditionallyUpdateMaxId(row)

      val neighbors = new Array[Int](numOutEdges)
      val weights = new Array[Double](numOutEdges)

      var i = 0
      while (i < numOutEdges) {
        val (columnStr, valueStr) = split(lines.next)
        val column = columnStr.toInt
        val value = valueStr.toDouble

        conditionallyUpdateMaxId(column)

        neighbors(i) = column
        weights(i) = value

        i += 1
      }

      WeightedNode(row, neighbors, normalize(weights))
    }

    val nodes = new mutable.ListBuffer[WeightedNode]()
    while(lines.hasNext) {
      nodes += readNextNode
    }

    PartialGraph(nodes, numEdges, maxId)
  }
}
