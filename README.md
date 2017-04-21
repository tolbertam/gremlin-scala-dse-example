# DataStax Enterprise Graph with gremlin-scala Example

This repository provides a basic example of using [gremlin-scala](https://github.com/mpollmeier/gremlin-scala) and 
the [Java driver for DataStax Enterprise](https://github.com/datastax/java-dse-driver) to interact with a DataStax
Enterprise Graph.

## How it works

The DSE driver includes a library `dse-java-driver-graph` for creating an Apache TinkerPop remote 
`GraphTraversalSource`.  You can read more about this integration on the 
[Apache TinkerPop client integration](http://docs.datastax.com/en/developer/java-driver-dse/latest/manual/tinkerpop/)
page in the driver manual:

```scala
val dseCluster = DseCluster.builder()
  .addContactPoint("127.0.0.1")
  .build()

val session = dseCluster.connect()

// Use DseGraph API to create a GraphTraversalSource from a previously connected session.
val g: GraphTraversalSource = DseGraph.traversal(session, graphOptions)
```

To enable `gremlin-scala`, all one needs to do is import it:

```scala
import gremlin.scala._
```

From here, the scala friendly function signatures provided from `gremlin-scala` are available on the driver-created
`GraphTraversalSource`.

Refer to [GremlinScalaApp.scala](./src/main/scala/am/tolbert/gremlin/GremlinScalaApp.scala) for a very basic example of how to accomplish
this.

## Workarounds needed to successfully build project

There are a few workarounds needed to use TinkerPop and the dse driver with scala that you should be aware of:

1. jBCrypt, a dependency of TinkerPop, does not resolve with default SBT settings.  This will be fixed in a future
TinkerPop release (see: [TINKERPOP-1633](https://issues.apache.org/jira/browse/TINKERPOP-1633)), for now [declaring
jitpack as a resolver](./build.sbt#L21) can be used as a workaround.

2. There is a weakness in the scala compiler that prevents code from using `DseCluster` from compiling.  To work around
this, [`-Ybreak-cycles` needs to be added to scalacOptions](./build.sbt#L9).  
See [JAVA-1252](https://datastax-oss.atlassian.net/browse/JAVA-1252) for more detail.

## Setting up a Local DSE Node for Testing

To run the tests, you first need to set up a local DSE node that is preconfigured.  The simplest way to accomplish this
is to use [ccm](https://github.com/pcmanus/ccm), but you may use an alternative means if you like.  The following steps demonstrate how to set up a single-node DSE ccm cluster with graph enabled:

1. Create the ccm cluster (with username and password being that which you use to download DSE normally):

    ```bash
    ccm create -n 1 -v 5.1.0 --dse dse510_1 --dse-username=username --dse-password=password
    ```

2. Enable the Graph workload:

   ```bash
   ccm setworkload graph
   ```

3. Start the ccm cluster

   ```bash
   ccm start
   ```
   
## Running the Example

To build and run the example, simply execute ```./sbt run``` which will compile and run
[GremlinScalaApp.scala](./src/main/scala/am/tolbert/gremlin/GremlinScalaApp.scala).  If everything goes well, the output should look like:

```
gremlin-scala-dse-example $ ./sbt run
[warn] Executing in batch mode.
[warn]   For better performance, hit [ENTER] to switch to interactive mode, or
[warn]   consider launching sbt without any commands, or explicitly passing 'shell'
[info] Loading project definition from /Users/atolbert/Documents/Projects/gremlin-scala-dse-example/project
[info] Set current project to gremlin-scala-dse-example (in build file:/Users/atolbert/Documents/Projects/gremlin-scala-dse-example/)
[info] Updating {file:/Users/atolbert/Documents/Projects/gremlin-scala-dse-example/}gremlin-scala-dse-example...
[info] Resolving jline#jline;2.14.1 ...
[info] Done updating.
[info] Compiling 1 Scala source to /Users/atolbert/Documents/Projects/gremlin-scala-dse-example/target/scala-2.12/classes...
[warn] Breaking cycle in base class computation of class Cluster in com.datastax.driver.core (List(class Cluster, trait Closeable, trait AutoCloseable, class Object, class Any))
[warn] one warning found
[info] Running am.tolbert.gremlin.GremlinScalaApp 
13:33:45.842 INFO  c.d.d.c.GuavaCompatibility - Detected Guava >= 19 in the classpath, using modern compatibility layer
13:33:46.015 INFO  c.d.d.c.ClockFactory     - Using native clock to generate timestamps.
13:33:46.063 INFO  a.t.g.GremlinScalaApp$   - Connecting to DSE
13:33:46.106 INFO  c.d.d.c.NettyUtil        - Did not find Netty's native epoll transport in the classpath, defaulting to NIO.
13:33:46.625 INFO  c.d.d.c.p.DCAwareRoundRobinPolicy - Using data-center name 'Graph' for DCAwareRoundRobinPolicy (if this is incorrect, please provide the correct datacenter name with DCAwareRoundRobinPolicy constructor)
13:33:46.628 INFO  c.d.d.c.Cluster          - New Cassandra host /127.0.0.1:9042 added
13:33:46.641 INFO  a.t.g.GremlinScalaApp$   - Creating graph example
13:33:46.847 INFO  a.t.g.GremlinScalaApp$   - Enabling development mode
13:33:46.891 INFO  a.t.g.GremlinScalaApp$   - Allowing scans
13:33:46.979 INFO  a.t.g.GremlinScalaApp$   - Creating vertex
13:33:47.273 INFO  a.t.g.GremlinScalaApp$   - Retrieving 'age' property of created Vertex
13:33:47.299 INFO  a.t.g.GremlinScalaApp$   - Response: {age=[29]}
13:33:47.299 INFO  a.t.g.GremlinScalaApp$   - Disconnecting
13:33:49.539 INFO  a.t.g.GremlinScalaApp$   - Done
[success] Total time: 11 s, completed Apr 19, 2017 1:33:50 PM
```
