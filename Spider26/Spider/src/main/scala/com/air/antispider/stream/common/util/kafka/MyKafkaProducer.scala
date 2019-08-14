package com.air.antispider.stream.common.util.kafka

import com.air.antispider.stream.common.util.jedis.PropertiesUtil
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}

/**
  * 自定义对象,实现序列化,为了将KafkaProducer放入广播变量
  */
class MyKafkaProducer(kafkaProducer: () => KafkaProducer[String, String]) extends Serializable {

  //lazy:用的时候再加载,不用的时候就是个引用
  lazy val producer = kafkaProducer()

  //自定义一个send方法,接收topic和消息,底层还是调用KafkaProducer的send方法
  def send(topic: String, message: String) = {
    producer.send(new ProducerRecord[String, String](topic, message))
  }
}

object MyKafkaProducer {
  //定义一个方法,能够获取MyKafkaProducer对象
  def getProducer(): MyKafkaProducer = {
    def producerFun = () => {
      val props = new java.util.HashMap[String, Object]()
      //设置 brokers
      props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,PropertiesUtil.getStringByKey("default.brokers", "kafkaConfig.properties"))
      //key 序列化方法
      props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,PropertiesUtil.getStringByKey("default.key_serializer_class_config", "kafkaConfig.properties"))
      //value 序列化方法
      props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,PropertiesUtil.getStringByKey("default.value_serializer_class_config", "kafkaConfig.properties"))
      //批发送设置：32KB 作为一批次或 10ms 作为一批次
      props.put(ProducerConfig.BATCH_SIZE_CONFIG,PropertiesUtil.getStringByKey("default.batch_size_config", "kafkaConfig.properties"))
      props.put(ProducerConfig.LINGER_MS_CONFIG,PropertiesUtil.getStringByKey("default.linger_ms_config", "kafkaConfig.properties"))
      //创建连接
      val producer = new KafkaProducer[String,String](props)
      //添加一个钩子,当程序停止的时候,需要做什么工作
      ////      sys.addShutdownHook({
      ////        producer.close()
      ////      })

      producer
    }


    //创建MyKafkaProducer对象
    new MyKafkaProducer(producerFun)
  }
}