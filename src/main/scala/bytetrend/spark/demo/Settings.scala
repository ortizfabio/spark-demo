package bytetrend.spark.demo

import com.typesafe.config.{Config, ConfigFactory}

import scala.util.Try


class Settings(val config: Config) {
  val appName = "spark-demo"
  val processorConfig = config.getConfig(appName)

  def getKafkaConnect: KafkaConnect = KafkaConnect(
    Try {
      Some(processorConfig.getString("kafkaConnect.group.id"))
    }.getOrElse(None),
    topic = processorConfig.getString("kafkaConnect.topic"),
    kafkaPort = processorConfig.getString("kafkaConnect.kafka.port"),
    kafkaHost = processorConfig.getString("kafkaConnect.kafka.host"),
    zkPort = Try {
      Some(processorConfig.getString("kafkaConnect.zk.port"))
    }.getOrElse(None),
    zkHost = Try {
      Some(processorConfig.getString("kafkaConnect.zk.host"))
    }.getOrElse(None)
  )

  def getOauth: TweeterCredentials = TweeterCredentials(
    processorConfig.getString("oauth.consumerKey"),
    processorConfig.getString("oauth.consumerSecret"),
    processorConfig.getString("oauth.accessToken"),
    processorConfig.getString("oauth.accessTokenSecret")
  )

  def queryList(): String = {
    import scala.collection.JavaConversions._

    val list = processorConfig.getStringList("twitterQuery.key")
    list.mkString("", ",", "")
  }
}

object Settings extends Settings(ConfigFactory.load)

case class KafkaConnect(
                         groupId: Option[String],
                         topic: String,
                         kafkaHost: String,
                         kafkaPort: String,
                         zkHost: Option[String] = None,
                         zkPort: Option[String] = None
                       )

case class TweeterCredentials(
                               consumerKey: String,
                               consumerSecret: String,
                               accessToken: String,
                               accessTokenSecret: String
                             )
