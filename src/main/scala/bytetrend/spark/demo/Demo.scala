package bytetrend.spark.demo


import akka.actor.{ActorSystem, Props}
import akka.camel.CamelExtension
import com.typesafe.config.ConfigFactory
import org.apache.camel.component.twitter.{TwitterComponent, TwitterEndpoint}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.{Seconds, StreamingContext}

import scala.concurrent.Await
import scala.concurrent.duration._

object Demo extends App {

  implicit val system = ActorSystem(Settings.appName, ConfigFactory.load().getConfig("spark-demo"))

  // val tweeter = new TweetterStreamer

  // val spark:SparkSession = SparkSession.builder().appName(Settings.appName).master("local[*]").getOrCreate()
  // val sc:SparkContext = spark.sparkContext
  val sparkConf = new SparkConf().setAppName(Settings.appName).setMaster("local[*]")
  val ssc = new StreamingContext(sparkConf, Seconds(120))

  val twitterSourceActor = system.actorOf(TwitterSourceActor.props, name = TwitterSourceActor.name)
  val analyticsActor = system.actorOf(Props(new SparkTwitterAnalyticEngine(ssc)), name = SparkTwitterAnalyticEngine.name)

  //Use system's dispatcher as ExecutionContext
  import system.dispatcher

  //This will schedule to send the TWITTER_DATA
  //to the analyticsActor after 2 minutes repeating every 50ms
  val scheduleAnalytics =
  system.scheduler.scheduleOnce(
    60000 milliseconds,
    analyticsActor,
    AnalyticEvent.TWITTER_DATA)


  //Add hook for graceful shutdown
  sys.addShutdownHook {
    //This cancels further messages to be sent
    ssc.awaitTermination()
    scheduleAnalytics.cancel()
    system.log.info("Shutting down")
    system.terminate()
    Await.result(system.whenTerminated, 500.millis)
    println(s"Actor system ${system.name} successfully shut down")

  }

  private[this] def doCamelSetup(system: ActorSystem): Unit = {
    import org.apache.camel.component.twitter.data.EndpointType
    val camel = CamelExtension(system)
    val camelContext = camel.context
    val tweeterCredentials = Settings.getOauth
    val tc = camelContext.getComponent("twitter", classOf[TwitterComponent])
    tc.setAccessToken(tweeterCredentials.accessToken)
    tc.setAccessTokenSecret(tweeterCredentials.accessTokenSecret)
    tc.setConsumerKey(tweeterCredentials.consumerKey)
    tc.setConsumerSecret(tweeterCredentials.consumerSecret)


  }

}
