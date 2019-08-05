package br.com.rnegrao.testepoc.rascunho

import java.util.Properties

import br.com.rnegrao.testepoc.cassandra.{CassandraConfig, CassandraSinkForeach}
import br.com.rnegrao.testepoc.twitter.TwitterConfig
import com.google.gson.GsonBuilder
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.spark.sql.streaming.OutputMode
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.apache.spark.sql.{Row, SaveMode, SparkSession}
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}
import twitter4j.FilterQuery
import twitter4j.auth.OAuthAuthorization
import twitter4j.conf.ConfigurationBuilder

import scala.util.Try

object TestePocRascunho1Stream extends TwitterConfig with CassandraConfig {

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

  def consumerTweets() = {

    println("**** consumerTweets()")

    val sparkSession = SparkSession
      .builder()
      .appName("teste-poc-kafka-to-cassandra")
      .config("spark.master", "local")
      .config("spark.driver.allowMultipleContexts", true)
      .getOrCreate()
    val sqlContext = sparkSession.sqlContext

    val tweetsKafkaStream = sparkSession
      .readStream
      .format("kafka")
      .option("subscribe", "tweets")
      .options(getKafkaParameters("grupo03", "localhost:9092"))
      .load()
      .selectExpr("CAST(value AS STRING)").as("value")

    val tweetsKafkaDF = tweetsKafkaStream
      .createOrReplaceTempView("raw_tweets")

    tweetsKafkaStream.createOrReplaceTempView("tweets")


    val top5followersTweetsViewDF = sqlContext.sql(
      """
        | SELECT user.name, user.followersCount
        |   FROM tweets
        |  ORDER BY user.followersCount DESC
        |  LIMIT 5
      """.stripMargin)

    top5followersTweetsViewDF
      .writeStream
      .foreach(new CassandraSinkForeach(sparkSession) {
        override def cql(record: Row): String = {
           s"""
               | insert testepoc.top5followersTweets(username, followersCount)
               | values ('${record(0)}', ${record(1)})"
             """.stripMargin
        }
      })
      .outputMode(OutputMode.Update())
      .start()

    val countTweetsByYyyyMmDdHhDf = sqlContext.sql(
      """
        | SELECT
        |      date_format(to_timestamp(createdAt, 'MMM dd, yyyy hh:mm:ss aaa'), 'yyyy-MM-dd HH') createdAt2,
        |      COUNT(1) countTweets
        |   FROM tweets
        | GROUP BY createdAt2
      """.stripMargin)


    countTweetsByYyyyMmDdHhDf
      .writeStream
      .foreach(new CassandraSinkForeach(sparkSession) {
        override def cql(record: Row): String = {
          s"""
             | insert testepoc.countTweets(createdAt, countTweets)
             | values ('${record(0)}', ${record(1)})"
             """.stripMargin
        }
      })
      .outputMode(OutputMode.Update())
      .start()

    val countHashtagsByYyyyMMDDHH = sqlContext.sql(
      """
        |   SELECT hashtag, count(1) as hashtag_count
        |     FROM tweets t
        |          LATERAL VIEW explode(split(regexp_replace(trim(upper(t.text)),"[^#A-Za-z0-9]"," "), ' ')) v1 as hashtag
        |    WHERE hashtag rlike "^#[a-zA-Z0-9]+$"
        | GROUP BY hashtag
        | ORDER BY hashtag_count desc LIMIT 10
      """.stripMargin)

    countHashtagsByYyyyMMDDHH
      .writeStream
      .foreach(new CassandraSinkForeach(sparkSession) {
        override def cql(record: Row): String = {
          s"""
             | insert testepoc.countHashtags(hashtag, countHashtag)
             | values ('${record(0)}', ${record(1)})"
             """.stripMargin
        }
      })
      .outputMode(OutputMode.Update())
      .start()
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
      val query = new FilterQuery();
      query.language("pt_BR")

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

    val sparkSession = SparkSession
      .builder()
      .appName("teste-poc-twitter-to-kafka")
      .config("spark.master", "local")
      .config("spark.driver.allowMultipleContexts", true)
      .getOrCreate()

    val sparkContext = sparkSession.sparkContext

    sparkContext.getConf
      .set("spark.cassandra.connection.host", hostCassandra)

    val streamingContext = new StreamingContext(sparkContext, Seconds(15))

    //    var args = Seq("lulalivre")

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

      val tweetsJsonDF = sparkSession.createDataFrame(jsonRDD, structType)

      tweetsJsonDF.printSchema()

      tweetsJsonDF
        .write
        .format("kafka")
        .mode(SaveMode.Append)
        .option("kafka.bootstrap.servers", "localhost:9092")
        .option("topic", "tweets")
        .save()

    })

    streamingContext.start()
    streamingContext.awaitTerminationOrTimeout(TWITTER_TIMEOUT)
//    streamingContext.addStreamingListener(new StreamingListener() {
//      override def onBatchCompleted(batchCompleted: StreamingListenerBatchCompleted) {
//        println("### onBatchCompleted")
//        consumerTweets()
//      }
//    })
  }

  def main(args: Array[String]): Unit = {

//    producerTweets(args)

    consumerTweets()

  }

}
