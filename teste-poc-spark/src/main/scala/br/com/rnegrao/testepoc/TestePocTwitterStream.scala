package br.com.rnegrao.testepoc

import java.text.SimpleDateFormat

import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.cassandra._
import org.apache.spark.sql.functions._
import org.apache.spark.SparkContext._
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.{Duration, Minutes, Seconds, StreamingContext}
import StreamingContext._
import br.com.rnegrao.testepoc.rascunho.TestePocRascunho1Stream.{twitterApiKey, twitterApiSecret, twitterTokenKey, twitterTokenSecret}
import br.com.rnegrao.testepoc.cassandra.CassandraConfig
import com.datastax.spark.connector.{SomeColumns, toRDDFunctions}
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}
import org.apache.spark.sql.{SaveMode, SparkSession}
import org.apache.spark.streaming.dstream.ReceiverInputDStream
import org.apache.spark.{SparkConf, SparkContext}
import twitter4j.{FilterQuery, Status}
import twitter4j.auth.OAuthAuthorization
import twitter4j.conf.{Configuration, ConfigurationBuilder}

import scala.io.Source

object TestePocTwitterStream extends App with CassandraConfig {

  // https://github.com/epishova/Structured-Streaming-Cassandra-Sink/blob/master/blog_draft.md
  // https://indatalabs.com/blog/convert-spark-rdd-to-dataframe-dataset
  // https://github.com/dimetron/apache-spark-streaming-twitter
  // https://hub.docker.com/_/datastax

  Logger.getRootLogger.setLevel(Level.ERROR)

  val spark = SparkSession
    .builder()
    .appName("teste-poc-twitter-cassandra")
    .config("spark.master", "local")
    .config("spark.driver.allowMultipleContexts", true)
    .config("spark.streaming.concurrentJobs", "1")
    .config("spark.cassandra.connection.host", hostCassandra)
    .config("spark.cassandra.connection.port", "9042")
//    .config("spark.sql.streaming.checkpointLocation", checkpointLocationCassandra)
    .getOrCreate()

  val sqlContext = spark.sqlContext

  val sc = spark.sparkContext

  sc.setLogLevel("ERROR")

  val isDebug = false

  val isSaveCassandra = true

  import spark.implicits._

  def coletarDados() = {

    // definindo o tempo de duracao de captura de dados do tweets
    val windowDuration: Duration = Seconds(60)

    val timeoutDuration = Seconds(60 * 5)

    val ssc = new StreamingContext(sc, Seconds(10))

    val tweets = getTweetsStream(ssc)

    debugTweets(tweets)

    agruparUsuariosComMaisSeguidores(windowDuration, tweets)

    agruparHashtagsMaisPopulares(windowDuration, tweets)

    agruparHashtagsPorHora(windowDuration, tweets)

    ssc.start()
    ssc.awaitTerminationOrTimeout(timeoutDuration.milliseconds)
    ssc.stop(true)

    if (sc != null) {
      sc.stop()
    }
  }

  private def getOAuthAuthorization = {
    val builder = new ConfigurationBuilder()
    builder.setOAuthConsumerKey(twitterApiKey)
    builder.setOAuthConsumerSecret(twitterApiSecret)
    builder.setOAuthAccessToken(twitterTokenKey)
    builder.setOAuthAccessTokenSecret(twitterTokenSecret)
    val configuration = builder.build()
    Some(new OAuthAuthorization(configuration))
  }

  def getFilters = {

//    var filters = args
    var filters = Seq("principais") // coloca um assunto polemico
    if (args.nonEmpty) {
      filters = args
    }
    val filterQuery = new FilterQuery().track(filters.mkString(","))
    Some(filterQuery)
  }

  def getTweetsStream(ssc: StreamingContext) = {
    if (getFilters.nonEmpty) {
      TwitterUtils.createFilteredStream(ssc, getOAuthAuthorization, getFilters, StorageLevel.MEMORY_ONLY_SER_2)
    } else {
      TwitterUtils.createStream(ssc, getOAuthAuthorization, args, StorageLevel.MEMORY_ONLY_SER_2)
    }

  }

  private def debugTweets(tweets: ReceiverInputDStream[Status]) = {

    if (isDebug) {
      tweets.foreachRDD(rdd => {
        println(s"Novos tweets: ${rdd.count()}")
      })
    }
  }

  import com.datastax.spark.connector.streaming._

  private def agruparUsuariosComMaisSeguidores(duration: Duration, tweets: ReceiverInputDStream[Status]) = {

    // recuperando a quantidade de usuarios com mais seguidores
    val users = tweets.map(status => (status.getUser().getScreenName(), status.getUser().getFollowersCount()))

    // preparando para imprimir os top 5 usuarios
    val usersReduced = users
      .reduceByKeyAndWindow(_ + _, duration)
      .map({ case (user, count) => (count, user) })
      .transform(_.sortByKey(false))

    usersReduced.foreachRDD(rdd => {
      println(s"Contagem reduzida de usuários = ${rdd.count()}")

      val topUsers = rdd.take(5)
      topUsers.foreach({
        case (count, user) => println(s"${user} (${count} followers)")
      })

      if (isSaveCassandra) {
        sc.parallelize(Seq(("top5", topUsers)))
          .toDF("id", "users")
          .write
          .cassandraFormat("top5followerstweets", "testepoc")
          .mode(SaveMode.Overwrite)
          .save()
      }
    })
  }

  private def agruparHashtagsMaisPopulares(duration: Duration, tweets: ReceiverInputDStream[Status]) = {

    // preparando para identificar as hashtags mais populares
    val hashTags = tweets
      .filter(_.getLang == "pt")
      .flatMap(status => status.getText.split(" ").filter(_.startsWith("#")))

    val topHashTags = hashTags
      .map((_, 1))
      .reduceByKeyAndWindow(_ + _, duration)
      .map({case (hashtag, count) => (count, hashtag)})
      .transform(_.sortByKey(false))

    if (isSaveCassandra) {
      topHashTags.saveToCassandra("testepoc", "counthashtags", SomeColumns("counttweets", "hashtag"))
    }

    topHashTags.foreachRDD(rdd => {
      println(s"\nHashtags mais populares nos últimos ${duration} segundos: (${rdd.count()} total)")

      rdd.foreach({
        case (count, tag) => println(s"${tag} (${count} tweets)")
      })
    })
  }

  private def agruparHashtagsPorHora(duration: Duration, tweets: ReceiverInputDStream[Status]) = {

    // preparando para agrupadas por hora do dia (independentemente da #hashtag)
    val topHashTagsByHours = tweets
      .filter(_.getLang == "pt")
      .map(status => (new SimpleDateFormat("yyyy-MM-dd HH").format(status.getCreatedAt), status.getText.split(" ").count(_.startsWith("#"))))
      .reduceByKeyAndWindow(_ + _, duration)
      .map({case (topic, count) => (count, topic)})
      .transform(_.sortByKey(false))

    if (isSaveCassandra) {
      topHashTagsByHours.saveToCassandra("testepoc", "counttweets", SomeColumns("counttweets", "createdat"))
    }

    topHashTagsByHours.foreachRDD(rdd => {
      println(s"\nQuantidade de postagem nos ${duration} segundos (${rdd.count()} total):")

      rdd.foreach({
        case (count, tag) => println(s"${tag} (${count} tweets)")
      })
    })
  }

  coletarDados()
}