package bytetrend.spark.demo


import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory

import scala.concurrent.Await
import scala.concurrent.duration._

object Demo extends App {

  implicit val system = ActorSystem("demo", ConfigFactory.load().getConfig("spark-demo"))

  val tweeter = new TweetterStreamer

  //Add hook for graceful shutdown
  sys.addShutdownHook {
    system.log.info("Shutting down")
    system.terminate()
    Await.result(system.whenTerminated, 500.millis)

    println(s"Actor system ${system.name} successfully shut down")

  }

}
