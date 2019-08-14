package com.air.antispider.stream.dataprocess.businessprocess

import com.air.antispider.stream.common.util.jedis.{JedisConnectionUtil, PropertiesUtil}
import org.apache.spark.rdd.RDD
import org.json4s.DefaultFormats
import org.json4s.jackson.Json
import redis.clients.jedis.JedisCluster

/**
  * 链路统计业务
  */
object LinkCount {
  def linkCount(messageRDD: RDD[String]):collection.Map[String, Int] = {

    //1.获取信息采集量数据
    //String => (ip, 1)
    val serverCountRDD: RDD[(String, Int)] = messageRDD.map(message => {
      //1.1 先切割message
      val arr: Array[String] = message.split("#CS#")
      //1.2  先判断长度
      if (arr.length > 9) {
        //1.3 如果长度没有问题,计数为1
        (arr(9), 1)
      } else {
        //取不出来IP
        ("", 1)
      }
    })
      //开始累加数据
      .reduceByKey(_ + _)

    //2.获取活跃连接数数据
    //string => (ip, 23),因为显示任意一条的活跃连接数都是可以的.所以我们就使用最后一条来展示
    val activeCountRDD: RDD[(String, Int)] = messageRDD.map(message => {
      //2.1 切割数据
      val arr: Array[String] = message.split("#CS#")
      //2.2 判断长度,有没有活跃连接数
      if (arr.length > 11) {
        //2.3 如果有,取出来,转换为元组
        //(ip, 活跃连接数)
        (arr(9), arr(11).toInt)
      } else {
        ("", 1)
      }
    })
      //取出最后一条
      // (x:当前的,y:下一个) => y
      .reduceByKey((x, y) => y)



    //将数据保存,可以保存在MySQL中,让前端去MySQL中获取,但是需要频繁操作MySQL.不太适合这个场景

    //3.将数据存入Redis

    //怎么存入Redis,什么形式?key是什么?CSANTI_MONITOR_LP
    //按照CSANTI_MONITOR_LP作为key,将数据封装为json存入redis.json的具体内容根据前端代码来实现.

    //将计算结果转换为 Map => json
//    {
//      serversCountMap: {"192.168.80.81": 23,"192.168.80.82":25}
//      activeNumMap:{}
//    }
    //3.1先判断数据是否为空,
    if (!serverCountRDD.isEmpty() && !activeCountRDD.isEmpty()) {
      //3.2 如果数据不为空,直接将数据采集为map.
      val serversCountMap: collection.Map[String, Int] = serverCountRDD.collectAsMap()
      val activeNumMap: collection.Map[String, Int] = activeCountRDD.collectAsMap()

      //3.3 将数据组装成一个json
      val map: Map[String, collection.Map[String, Int]] = Map[String, collection.Map[String, Int]](
        "serversCountMap" -> serversCountMap,
        "activeNumMap" -> activeNumMap
      )
      //使用Jackson将数据转换为json
      val json: String = Json(DefaultFormats).write(map)

      //3.4 将数据存入Redis
      //创建Redis连接
      val jedis: JedisCluster = JedisConnectionUtil.getJedisCluster
      val key: String = PropertiesUtil.getStringByKey("cluster.key.monitor.linkProcess", "jedisConfig.properties") + System.currentTimeMillis()
      val ex: Int = PropertiesUtil.getStringByKey("cluster.exptime.monitor", "jedisConfig.properties").toInt

      //存入数据
      jedis.setex(key, ex, json)
    }
    //将服务器数据采集量返回,用于状态监控
    serverCountRDD.collectAsMap()

  }

}
