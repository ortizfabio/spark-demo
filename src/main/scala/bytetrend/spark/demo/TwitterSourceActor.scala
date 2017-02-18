package bytetrend.spark.demo

import java.util.concurrent.TimeUnit

import akka.actor.{ActorLogging, ActorRef, Props}
import akka.camel.{CamelMessage, Consumer}
import akka.pattern.Patterns
import akka.stream.ActorMaterializer
import akka.util.Timeout
import twitter4j.Status

object TwitterSourceActor {
  val name = TwitterSourceActor.getClass.getSimpleName

  def props: Props = Props(classOf[TwitterSourceActor])

}

/**
  * Twitter / camel-twitter
  * twitter://endpoint[?options]
  * A twitter endpoint
  */
class TwitterSourceActor extends Consumer with ActorLogging {
  implicit val materializer = ActorMaterializer()

  val kafkaProducer: ActorRef = context.actorOf(KafkaTweetProducer.props, KafkaTweetProducer.name + "$")

  private val tweeterCredentials = Settings.getOauth

  override def autoAck = true

  def endpointUri: String = s"twitter://streaming/filter?type=event&keywords=${Settings.queryList()}&consumerKey=${tweeterCredentials.consumerKey}&consumerSecret=${tweeterCredentials.consumerSecret}&accessToken=${tweeterCredentials.accessToken}&accessTokenSecret=${tweeterCredentials.accessTokenSecret}"

  override def receive: Receive = {
    case camelMessage: CamelMessage => {

      val tweet: Status = camelMessage.bodyAs[Status]
      println(tweet)

      //    println(s"${tweet.created_at} ${tweet.text}")
      val t = new Timeout(5, TimeUnit.SECONDS)
      val fut = Patterns.ask(kafkaProducer, tweet, t)

      sender() ! "<confirm>OK</confirm>"

    }
  }
}
