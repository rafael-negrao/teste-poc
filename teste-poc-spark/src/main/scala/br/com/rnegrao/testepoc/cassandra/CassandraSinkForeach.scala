package br.com.rnegrao.testepoc.cassandra

import org.apache.spark.sql.{ForeachWriter, Row, SparkSession};

abstract class CassandraSinkForeach(sparkSession: SparkSession) extends ForeachWriter[Row] {

  var cassandraDriver: CassandraDriver = null;
  def open(partitionId: Long, version: Long): Boolean = {
    println(s"Open connection")
    true
  }

  def process(record: org.apache.spark.sql.Row) = {
    println(s"Process new $record")
    if (cassandraDriver == null) {
      cassandraDriver = new CassandraDriver(sparkSession);
    }
    cassandraDriver.connector.withSessionDo(session =>
      session.execute(cql(record))
    )
  }

  def close(errorOrNull: Throwable): Unit = {
    // close the connection
    println(s"Close connection")
  }

  def cql(record: org.apache.spark.sql.Row): String
}
