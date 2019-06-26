package br.com.rnegrao.testepoc.cassandra

import com.datastax.spark.connector.cql.CassandraConnector
import org.apache.spark.sql.SparkSession

class CassandraDriver(sparkSession: SparkSession) {

  val connector = CassandraConnector(sparkSession.sparkContext.getConf)

}