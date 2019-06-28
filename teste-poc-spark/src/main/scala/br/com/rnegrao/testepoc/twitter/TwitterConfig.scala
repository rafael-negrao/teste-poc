package br.com.rnegrao.testepoc.twitter

import java.io.InputStream
import java.util.Properties

trait TwitterConfig {

  val twitterProperties: Properties = {
    val properties = new Properties
    val propertiesInputStream: InputStream = this.getClass.getClassLoader.getResourceAsStream("twitter4j.properties")
    properties.load(propertiesInputStream)
    properties
  }

  val twitterApiKey = twitterProperties.getProperty("oauth.consumerKey")
  val twitterApiSecret = twitterProperties.getProperty("oauth.consumerSecret")
  val twitterTokenKey = twitterProperties.getProperty("oauth.accessToken")
  val twitterTokenSecret = twitterProperties.getProperty("oauth.accessTokenSecret")

}
