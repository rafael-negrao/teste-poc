package br.com.rnegrao.testepoc.rascunho

import java.util.Properties

import br.com.rnegrao.testepoc.cassandra.CassandraConfig
import br.com.rnegrao.testepoc.twitter.TwitterConfig
import com.google.gson.GsonBuilder
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}
import twitter4j.auth.OAuthAuthorization
import twitter4j.conf.ConfigurationBuilder

import scala.util.Try

object TestePoc2Rascunho2Stream extends TwitterConfig with CassandraConfig {

  val TWITTER_TIMEOUT = 1000 * 20 * 1

  val KAFKA_TIMEOUT = 1000 * 5 * 1

  def getKafkaParameters(groupId: String, servers: String): Map[String, String] = {
    val kafkaProperties: Properties = getKafkaProperties(groupId, servers)
    import scala.collection.JavaConverters._
    kafkaProperties.asScala.mapValues(_.asInstanceOf[String]).toMap
  }

  def getKafkaProperties(groupId: String, servers: String): Properties = {
    val kafkaParams = Map[String, Object](
      "auto.offset.reset" -> "earliest",
      "kafka.bootstrap.servers" -> servers,
      ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> servers,
      ConsumerConfig.GROUP_ID_CONFIG -> groupId,
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> "org.apache.kafka.common.serialization.StringDeserializer",
      ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> "org.apache.kafka.common.serialization.StringDeserializer")
    val kafkaProperties = new Properties
    kafkaParams.foreach(p => kafkaProperties.put(p._1, p._2))
    kafkaProperties
  }

  // https://github.com/epishova/Structured-Streaming-Cassandra-Sink/blob/master/blog_draft.md
  // referencia: https://medium.com/@michaeljpr/five-minute-guide-getting-started-with-cassandra-on-docker-4ef69c710d84
  // referencia: https://www.programcreek.com/scala/org.apache.spark.streaming.twitter.TwitterUtils

  def getTwitterStream(streamingContext: StreamingContext, filters: Seq[String] = Nil) = {
    val builder = new ConfigurationBuilder()
    builder.setOAuthConsumerKey(twitterApiKey)
    builder.setOAuthConsumerSecret(twitterApiSecret)
    builder.setOAuthAccessToken(twitterTokenKey)
    builder.setOAuthAccessTokenSecret(twitterTokenSecret)
    val configuration = builder.build()

    if (filters.nonEmpty) {
      TwitterUtils.createStream(
        streamingContext,
        Some(new OAuthAuthorization(configuration)),
        filters,
        StorageLevel.MEMORY_ONLY
      )
    } else {
      TwitterUtils.createStream(
        streamingContext,
        Some(new OAuthAuthorization(configuration)),
        filters,
        StorageLevel.MEMORY_ONLY
      )
    }
  }

  private def producerTweets(args: Array[String]) = {

    println("**** producerTweets()")

    val spark = SparkSession
      .builder()
      .appName("teste-poc-twitter-to-kafka")
      .config("spark.master", "local")
      .config("spark.driver.allowMultipleContexts", true)
      .getOrCreate()

    val sqlContext = spark.sqlContext

    val sparkContext = spark.sparkContext

    sparkContext.setLogLevel("ERROR")

    sparkContext.getConf
      .set("spark.cassandra.connection.host", hostCassandra)

    val streamingContext = new StreamingContext(sparkContext, Seconds(15))

    // var args = Seq("lulalivre")

    val twitterStream = getTwitterStream(streamingContext, args)
    twitterStream.foreachRDD(statusRDD => {

      val jsonTryDS = statusRDD.map(status => {
        val gson = new GsonBuilder().create()
        Try({
          val json = gson.toJson(status)
          println(s"json = '${json}'")
          json
        })
      })

      val jsonRDD = jsonTryDS
        .filter(_.isSuccess)
        .map(_.get)
        // .filter(json => isNotEmpty(json) && isNotBlank(json))
        .map(json => Row(json))

      val structType = StructType(StructField("value", StringType, true) :: Nil)

      val tweetsJsonDF = spark.createDataFrame(jsonRDD, structType)

      tweetsJsonDF.printSchema()



    })

    streamingContext.start()
    streamingContext.awaitTerminationOrTimeout(TWITTER_TIMEOUT)
  }

  def main(args: Array[String]): Unit = {

    producerTweets(args)

  }

}
