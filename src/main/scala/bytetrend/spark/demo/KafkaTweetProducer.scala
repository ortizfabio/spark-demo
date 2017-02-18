package bytetrend.spark.demo

import akka.Done
import akka.actor.{Actor, ActorLogging, Props}
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}
import twitter4j.Status
import scala.concurrent.Future

object KafkaTweetProducer {
  def props: Props = Props(classOf[KafkaTweetProducer])

  def name: String = this.getClass.getSimpleName
}

class KafkaTweetProducer extends Actor with ActorLogging {

  val producerSettings = ProducerSettings(context.system, new ByteArraySerializer, new StringSerializer)
    .withBootstrapServers(s"${Settings.getKafkaConnect.kafkaHost}:${Settings.getKafkaConnect.kafkaPort}")
  val mapper = new ObjectMapper

  override def receive: Receive = {
    case tweet: Status =>
      implicit val materializer = ActorMaterializer()
      //      val done =
      val source = Source(tweet :: Nil)
        .map { elem =>
          new ProducerRecord[Array[Byte], String](Settings.getKafkaConnect.topic, mapper.valueToTree(elem).toString)
        }
      val done: Future[Done] = source.runWith(Producer.plainSink(producerSettings))
      log.debug(tweet.toString)
  }
}
