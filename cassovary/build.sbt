import AssemblyKeys._

organization := "net.joshdevins.pagerank"

name := "cassovary"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.9.2"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-optimise")

seq(assemblySettings: _*)

// dependencies - main
libraryDependencies ++= Seq(
  "com.twitter" %% "cassovary" % "3.0.0",
  "com.google.guava" % "guava" % "14.0.1" // transitive dependency from cassovary fails to download src; upgrading supercedes this dependency and resolves the problem
)

// dependencies - test
libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "1.8" % "test"
)

jarName in assembly := "pagerank-cassovary.jar"

mainClass in assembly := None

test in assembly := {}
