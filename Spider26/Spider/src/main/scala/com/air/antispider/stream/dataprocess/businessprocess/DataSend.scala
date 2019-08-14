package com.air.antispider.stream.dataprocess.businessprocess

import com.air.antispider.stream.common.bean.ProcessedData
import com.air.antispider.stream.common.util.jedis.PropertiesUtil
import com.air.antispider.stream.common.util.kafka.MyKafkaProducer
import kafka.producer.{KeyedMessage, Producer}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD

/**
  * 数据推送
  */
object DataSend {

  /**
    * 将数据发送给Kafka
    *
    * @param processedDataRDD
    */
  def sendDataToKafka(processedDataRDD: RDD[ProcessedData], producerBroadcast: Broadcast[MyKafkaProducer]) = {
    //发送查询数据
    sendQuery(0, processedDataRDD, producerBroadcast)
    //发送预订数据
    sendQuery(1, processedDataRDD, producerBroadcast)
  }

  /**
    * 按照指定的类型,将数据发送给Kafka
    * @param topicType
    * @param processedDataRDD
    */
  def sendQuery(topicType: Int, processedDataRDD: RDD[ProcessedData], producerBroadcast: Broadcast[MyKafkaProducer]) = {
    //发送的topic,默认为查询
    var topicName = PropertiesUtil.getStringByKey("target.query.topic", "kafkaConfig.properties")
    if (topicType == 1) {
      //如果需要向预订中发送消息,就修改topicName
      topicName = PropertiesUtil.getStringByKey("target.book.topic", "kafkaConfig.properties")
    }

    //先找到指定的类型.数据的过滤
    processedDataRDD.filter(processedData => {
      //看这条信息的操作类型和传入的topic类型是否一致
      processedData.requestType.behaviorType.id == topicType
    })
    //开始发送数据,按照分区发送数据
      .foreachPartition(it => {
      //创建 map 封装 kafka 参数
      //遍历迭代器中的所有数据
      it.foreach(data => {
        //将ProcessedData转换成字符串
        //1. JSON
        //2. 使用字符串拼接"#CS#"
        val message: String = data.toKafkaString()
        producerBroadcast.value.send(topicName, message)
      })

    })
  }


}
