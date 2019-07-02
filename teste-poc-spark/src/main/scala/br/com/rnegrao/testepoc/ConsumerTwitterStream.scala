package br.com.rnegrao.testepoc

import java.text.SimpleDateFormat

import br.com.rnegrao.testepoc.cassandra.CassandraConfig
import br.com.rnegrao.testepoc.rascunho.TestePocRascunho1Stream.{twitterApiKey, twitterApiSecret, twitterTokenKey, twitterTokenSecret}
import com.datastax.spark.connector.SomeColumns
import com.datastax.spark.connector.cql.CassandraConnector
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.cassandra._
import org.apache.spark.sql.{SaveMode, SparkSession}
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.{Duration, Minutes, Seconds, StreamingContext}
import twitter4j.auth.OAuthAuthorization
import twitter4j.conf.ConfigurationBuilder
import twitter4j.{FilterQuery, Status}



/**
  * @args
  *      <ul>
  *        <li>Primeiro Argumento: hashtag a ser pesquisada no twitter</li>
  *        <li>Segundo Argumento: processo a ser executado, seguem as opções:
  *          <ul>
  *            <li>USUARIOS_COM_MAIS_SEGUIDORES</li>
  *            <li>HASHTAGS_MAIS_POPULARES</li>
  *            <li>HASHTAGS_POR_HORA</li>
  *          </ul>
  *        </li>
  * </ul>
  */
object ConsumerTwitterStream extends App with CassandraConfig {

  // https://databricks-prod-cloudfront.cloud.databricks.com/public/4027ec902e239c93eaaa8714f173bcfc/3987486811597229/2090476422929802/7095385990720558/latest.html
  // https://github.com/epishova/Structured-Streaming-Cassandra-Sink/blob/master/blog_draft.md
  // https://indatalabs.com/blog/convert-spark-rdd-to-dataframe-dataset
  // https://github.com/dimetron/apache-spark-streaming-twitter
  // https://hub.docker.com/_/datastax

  Logger.getRootLogger.setLevel(Level.ERROR)

  val spark = SparkSession
    .builder()
    .appName("teste-poc-twitter-cassandra")
    .config("spark.master", "local[4]")
    .config("spark.driver.allowMultipleContexts", true)
    .config("spark.cassandra.connection.host", hostCassandra)
    .config("spark.cassandra.connection.port", "9042")
    .getOrCreate()

  val sqlContext = spark.sqlContext

  val sc = spark.sparkContext

  val connector = CassandraConnector(sc.getConf)

  sc.setLogLevel("ERROR")

  val isDebug = false

  val isSaveToCassandra = true

  import spark.implicits._

  def coletarDados() = {

    // definindo o tempo de duracao de captura de dados do tweets
    val windowDuration: Duration = Seconds(30)

    val slideDuration: Duration = Seconds(1)

    val timeoutDuration = Minutes(1)

    val ssc = new StreamingContext(sc, Seconds(1))

    val tweets = getTweetsStream(ssc)

    val tweetsLangPt = tweets.filter(_.getLang == "pt")

    debugTweets(tweetsLangPt)

    args(1).toUpperCase match {
      case "USUARIOS_COM_MAIS_SEGUIDORES" => agruparUsuariosComMaisSeguidores(windowDuration, slideDuration, tweetsLangPt)
      case "HASHTAGS_MAIS_POPULARES" => agruparHashtagsMaisPopulares(windowDuration, slideDuration, tweetsLangPt)
      case "HASHTAGS_POR_HORA" => agruparHashtagsPorHora(windowDuration, slideDuration, tweetsLangPt)
    }

    tweets.print()

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
    var filters = Seq("globo") // coloca um assunto polemico
    if (args.length > 1) {
      filters = Seq(args(0))
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

  private def debugTweets(tweets: DStream[Status]) = {
    if (isDebug) {
      tweets.foreachRDD(rdd => {
        println(s"Novos tweets: ${rdd.count()}")
      })
    }
  }

  import com.datastax.spark.connector.streaming._

  private def agruparUsuariosComMaisSeguidores(duration: Duration, slideDuration: Duration, tweets: DStream[Status]) = {

    // recuperando a quantidade de usuarios com mais seguidores
    val users = tweets
      .window(duration)
      .map(status => (status.getUser().getFollowersCount(), status.getUser().getScreenName()))
      .transform(_.sortByKey(false))

    users.foreachRDD(rdd => {
      val topUsers = rdd.take(5)
      if (isSaveToCassandra) {

        sc.parallelize(Seq(("top5", topUsers.toMap)))
          .toDF("id", "users")
          .write
          .cassandraFormat("top5followerstweets", "testepoc")
          .mode(SaveMode.Append)
          .save()

      } else {
        println(s"Contagem reduzida de usuários = ${rdd.count()}")
        topUsers.foreach({
          case (count, user) => println(s"${user} (${count} followers)")
        })
      }
    })
  }

  private def agruparHashtagsMaisPopulares(duration: Duration, slideDuration:Duration, tweets: DStream[Status]) = {

    // preparando para identificar as hashtags mais populares
    val hashTags = tweets
      .flatMap(status => status.getText().split(" ").filter(_.startsWith("#")))

    val topHashTags = hashTags
      .map((_, 1))
      .reduceByKeyAndWindow((x: Int, y: Int) => x + y, duration)
      .map({case (hashtag, count) => (count, hashtag)})
      .transform(_.sortByKey(false))

    if (isSaveToCassandra) {

      topHashTags.saveToCassandra("testepoc", "counthashtags", SomeColumns("counttweets", "hashtag"))

    } else {

      topHashTags.foreachRDD(rdd => {
        println(s"\nHashtags mais populares nos últimos ${duration} segundos: (${rdd.count()} total)")

        rdd.foreach({
          case (count, tag) => println(s"${tag} (${count} tweets)")
        })
      })

    }

  }

  private def agruparHashtagsPorHora(duration: Duration, slideDuration:Duration, tweets: DStream[Status]) = {

    // preparando para agrupadas por hora do dia (independentemente da #hashtag)
    val topHashTagsByHours = tweets
      .map(status => (new SimpleDateFormat("yyyy-MM-dd HH").format(status.getCreatedAt), status.getText.split(" ").count(_.startsWith("#"))))
      .reduceByKeyAndWindow((x: Int, y: Int) => x + y, duration)
      .map({case (topic, count) => (count, topic)})
      .transform(_.sortByKey(false))

    if (isSaveToCassandra) {

      topHashTagsByHours.saveToCassandra("testepoc", "counttweets", SomeColumns("counttweets", "createdat"))

    } else {

      topHashTagsByHours.foreachRDD(rdd => {
        println(s"\nQuantidade de postagem nos ${duration} segundos (${rdd.count()} total):")

        rdd.foreach({
          case (count, tag) => println(s"${tag} (${count} tweets)")
        })
      })
    }
  }

  coletarDados()
}