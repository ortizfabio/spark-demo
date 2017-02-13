package bytetrend.spark.demo

import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.duration.FiniteDuration
import scala.util.Try


class Settings(val config: Config) {
  val processorConfig = config.getConfig("spark-demo")

  import processorConfig._

  def getKafkaConnect(): KafkaConnect = KafkaConnect(
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

  def getOauth(): TweeterCredentials = TweeterCredentials(
    processorConfig.getString("oauth.consumerKey"),
    processorConfig.getString("oauth.consumerSecret"),
    processorConfig.getString("oauth.accessToken"),
    processorConfig.getString("oauth.accessTokenSecret")
  )
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
                               accessTokenSecret:String
                             )
