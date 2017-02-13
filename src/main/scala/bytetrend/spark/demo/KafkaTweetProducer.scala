package bytetrend.spark.demo

import java.util.Properties

import akka.Done
import akka.actor.{Actor, ActorLogging, Props}
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.ByteArraySerializer
import org.apache.kafka.connect.json.JsonSerializer

import scala.concurrent.Future

object KafkaTweetProducer {
  def props: Props = Props(classOf[KafkaTweetProducer])

  def name: String = this.getClass.getSimpleName
}

class KafkaTweetProducer extends Actor with ActorLogging {

  val producerSettings = ProducerSettings(context.system, new ByteArraySerializer, new JsonSerializer)
    .withBootstrapServers(s"${Settings.getKafkaConnect.kafkaHost}:${Settings.getKafkaConnect.kafkaPort}")
  val mapper = new ObjectMapper

  override def receive: Receive = {
    case tweet: Tweet =>
      implicit val materializer = ActorMaterializer()
      //      val done =
      val source = Source(tweet :: Nil)
        .map { elem =>
          new ProducerRecord[Array[Byte], JsonNode](Settings.getKafkaConnect.topic, mapper.valueToTree(elem))
        }
      val done: Future[Done] = source.runWith(Producer.plainSink(producerSettings))
      println(tweet)

  }
}
