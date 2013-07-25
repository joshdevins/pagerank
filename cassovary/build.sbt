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

// Hadoop core minus the runtime dep's we don't need
ivyXML :=
  <dependency org="org.apache.hadoop" name="hadoop-core" rev="0.20.2-cdh3u1">
      <exclude org="com.cloudera.cdh" module="hadoop-ant" />
      <exclude org="commons-cli" module="commons-cli" />
      <exclude org="commons-el" module="commons-el" />
      <exclude org="commons-codec" module="commons-codec" />
      <exclude org="commons-net" module="commons-net" />
      <exclude org="org.mortbay.jetty" module="jetty" />
      <exclude org="org.mortbay.jetty" module="jetty-util" />
      <exclude org="tomcat" module="jasper-runtime" />
      <exclude org="tomcat" module="jasper-compiler" />
      <exclude org="javax.servlet" module="servlet-api" />
      <exclude org="javax.servlet.jsp" module="jsp-api" />
      <exclude org="net.java.dev.jets3t" module="jets3t" />
      <exclude org="hsqldb" module="hsqldb" />
      <exclude org="oro" module="oro" />
      <exclude org="org.eclipse.jdt" module="core" />
      <exclude org="xmlenc" module="xmlenc" />
  </dependency>

resolvers ++= Seq(
  "Twitter" at "http://maven.twttr.com"
)

jarName in assembly := "pagerank-cassovary.jar"

mainClass in assembly := None

test in assembly := {}
