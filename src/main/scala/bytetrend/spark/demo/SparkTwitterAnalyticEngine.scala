package bytetrend.spark.demo

import akka.actor.{Actor, ActorLogging, Props}
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import kafka.serializer.StringDecoder
import twitter4j.Status
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.kafka010.KafkaUtils
import org.apache.spark.streaming.{Duration, Seconds, StreamingContext}
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe

object AnalyticEvent extends Enumeration {
  type Event = Value

  val TWITTER_DATA = Value

}

object SparkTwitterAnalyticEngine {
  def props: Props = Props(classOf[SparkTwitterAnalyticEngine])

  def name: String = this.getClass.getSimpleName

  val kafka = Settings.getKafkaConnect
  val kafkaParams = Map[String, Object](
    "bootstrap.servers" -> s"${kafka.kafkaHost}:${kafka.kafkaPort}",
    "key.deserializer" -> classOf[StringDeserializer],
    "value.deserializer" -> classOf[StringDeserializer],
    "group.id" -> kafka.groupId.getOrElse(throw new IllegalArgumentException("missing group.id in Kafka configuration.")),
    "auto.offset.reset" -> "latest",
    "enable.auto.commit" -> (false: java.lang.Boolean)
  )

  val topics = Array(kafka.topic)
}

class SparkTwitterAnalyticEngine(ssc: StreamingContext) extends Actor with ActorLogging {


  override def receive: Receive = {
    //if (event == AnalyticEvent.TWITTER_DATA)
    case _ => {
      TweetAnalyzer(ssc)
    }
  }

  case class TweetAnalyzer(ssc: StreamingContext) extends Serializable {
    //  ssc.checkpoint("checkpoint")

    // Create direct kafka stream with brokers and topics
    val stream = KafkaUtils.createDirectStream[String, String](
      ssc,
      PreferConsistent,
      Subscribe[String, String](SparkTwitterAnalyticEngine.topics, SparkTwitterAnalyticEngine.kafkaParams)
    )

    // Get the tweets
    val stream2 = stream.map(x => {
      val o = (new ObjectMapper).readTree(x.value())
     // o.asInstanceOf[Status]
    }
    ).print
    //stream.count().print()
    //  val stream: DStream[Status] = stringStream.map(x => mapper.readValue(x._2, Status.getClass).asInstanceOf[Status])
    // stream.window(Duration(600000))

    ssc.start()
    ssc.awaitTermination()
    //ssc.stop(false)
  }

}