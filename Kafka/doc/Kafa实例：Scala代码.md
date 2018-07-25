##### Kafa实例：Scala代码

- 生产者

  ```
  import java.util.Properties

  import kafka.producer.{KeyedMessage, Producer, ProducerConfig}

  /**
    * 创建一个Producer用于模拟生成数据，把数据发送到Kafka集群
    * 自定义分区器
    */
  object KafkaProducer {
    def main(args: Array[String]): Unit = {

      // 定义一个接收数据的topic
      val topic = "test1"

      // 创建一个配置文件类
      val props = new Properties()
      // 选择序列化数据类
      props.put("serializer.class", "kafka.serializer.StringEncoder")
      // 指定kafka集群列表
      props.put("metadata.broker.list", "node01:9092,node02:9092,node03:9092")
      // 设置发送数据后的响应方式：0，1，-1
      props.put("request.required.acks", "1")
      // 调用分区器
      //    props.put("partitioner.class", "kafka.producer.DefaultPartitioner")
      // 自定义分区器
      props.put("partitioner.class", "com.qf.gp1705.day11.ProducerPartitioner")

      // 实例化Producer配置类
      val config = new ProducerConfig(props)

      // 创建一个生产者实例
      val producer: Producer[String, String] = new Producer(config)

      // 模拟生产一些数据
      for (i <- 1 to 10000){
        val msg = s"${i} : Producer send data"
        producer.send(new KeyedMessage[String, String](topic, msg))
      }


    }
  }

  ```

  ​

- 自定义partition分区器

  ```
  import kafka.producer.Partitioner
  import kafka.utils.VerifiableProperties


  class ProducerPartitioner(props: VerifiableProperties) extends Partitioner{
    override def partition(key: Any, numPartitions: Int): Int = {
      key.hashCode() % numPartitions
    }

  }

  ```

  ​

- 消费者

```
import java.util.Properties
import java.util.concurrent.Executors

import kafka.consumer.{Consumer, ConsumerConfig, ConsumerIterator, KafkaStream}

import scala.collection.mutable

/**
  * 创建Consumer用于获取Kafka的数据
  * @param consumer
  * @param stream
  */
class KafkaConsumer(val consumer: String, val stream: KafkaStream[Array[Byte], Array[Byte]]) extends Runnable{
  override def run() = {
    val it: ConsumerIterator[Array[Byte], Array[Byte]] = stream.iterator()
    while (it.hasNext()) {
      val data = it.next()
      val topic = data.topic
      val partition = data.partition
      val offset = data.offset
      val msg = new String(data.message())

      println(s"Consumer: $consumer, topic: $topic" +
        s", Partition: $partition, Offset: $offset, message: $msg")
    }
  }
}
object KafkaConsumer {
  def main(args: Array[String]): Unit = {
    // 定义要获取的topic
    val topic = "test1"
    // 定义map，用于存储多个topic
    val topics= new mutable.HashMap[String, Int]()
    topics.put(topic, 2)

    // 配置信息
    val props = new Properties()
    // ConsumerGroup ID
    props.put("group.id", "group01")
    // 指定zk列表
    props.put("zookeeper.connect", "node01:2181,node02:2181,node03:2181")
    // 指定offset值
    props.put("auto.offset.reset", "smallest")

    // 调用Consumer配置类
    val config = new ConsumerConfig(props)
    // 创建Consumer实例，该实例在获取数据时，如果没有获取到数据，会一直线程等待
    val consumer = Consumer.create(config)

    // 获取数据，Map中的key代表topic的名称
    val steams: collection.Map[String, List[KafkaStream[Array[Byte], Array[Byte]]]] =
      consumer.createMessageStreams(topics)

    // 获取指定topic的数据
    val stream: Option[List[KafkaStream[Array[Byte], Array[Byte]]]] = steams.get(topic)

    // 创建一个固定大小的线程池
    val pool = Executors.newFixedThreadPool(3)

    for (i <- 0 until stream.size) {
      pool.execute(new KafkaConsumer(s"Consumer:$i", stream.get(i)))
    }

  }
}

```

