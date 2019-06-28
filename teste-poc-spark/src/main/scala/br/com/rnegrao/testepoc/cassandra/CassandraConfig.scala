package br.com.rnegrao.testepoc.cassandra

import java.io.InputStream
import java.util.Properties

trait CassandraConfig {

  val cassandraProperties: Properties = {
    val properties = new Properties
    val propertiesInputStream: InputStream = this.getClass.getClassLoader.getResourceAsStream("cassandra.properties")
    properties.load(propertiesInputStream)
    properties
  }

  val hostCassandra = cassandraProperties.getProperty("spark.cassandra.connection.host")
  val checkpointLocationCassandra = cassandraProperties.getProperty("spark.sql.streaming.checkpointLocation")

}