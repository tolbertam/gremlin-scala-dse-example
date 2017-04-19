package am.tolbert.gremlin

import com.datastax.driver.dse.graph.{GraphOptions, SimpleGraphStatement}
import com.datastax.driver.dse.{DseCluster, DseSession}
import com.datastax.dse.graph.api.DseGraph
import com.google.common.collect.ImmutableMap
import gremlin.scala._
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource
import org.slf4j.LoggerFactory

object GremlinScalaApp extends App {

  val logger = LoggerFactory.getLogger(GremlinScalaApp.getClass)

  val graphName = "example"
  val graphOptions = new GraphOptions().setGraphName(graphName)
  // Build a Cluster instance and declare configuration
  val dseCluster = DseCluster.builder()
    .addContactPoint("127.0.0.1")
    .build()

  try {
    logger.info("Connecting to DSE")
    val session = dseCluster.connect()
    createGraph(session)

    // Use DseGraph API to create a GraphTraversalSource from a previously connected session.
    val g: GraphTraversalSource = DseGraph.traversal(session, graphOptions)

    logger.info("Creating vertex")
    // very basic example that creates a vertex.  Calling next() should fire off the traversal and return a
    // 'DetachedVertex' of what was created.
    val marko: Vertex = g.addV("person").property("name", "marko").property("age", 29).next()

    logger.info("Retrieving 'age' property of created Vertex")
    // Retrieve said vertex's 'age' value by id and process using headOption which returns the first
    // matching (if found).
    val v = g.V(marko.id()).valueMap[Int]("age").headOption()

    v match {
      case Some(m) => logger.info("Response: {}", m)
      case None => logger.warn("Vertex not found")
    }
  } finally {
    logger.info("Disconnecting")
    dseCluster.close();
    logger.info("Done")
  }

  def createGraph(session: DseSession): Unit = {
    // Use the string-based API, since the tinkerpop fluent API doesn't support graph configuration

    // Create the graph if it doesn't exist.
    logger.info("Creating graph {}", graphName)
    session.executeGraph("system.graph(name).ifNotExists().create()", ImmutableMap.of("name", graphName))

    // Note: typically you would not want to use development mode and allow scans, but it is good for convenience
    // and experimentation during development.

    logger.info("Enabling development mode")
    // set the graph schema to development mode, which creates tables on the fly.
    session.executeGraph(new SimpleGraphStatement("schema.config().option('graph.schema_mode').set('development')").setGraphName(graphName))
    logger.info("Allowing scans")
    // allow scans to bypass need for indices on data we are querying.
    session.executeGraph(new SimpleGraphStatement("schema.config().option('graph.allow_scan').set('true')").setGraphName(graphName))
  }

}
