package com.air.antispider.stream.dataprocess

import com.air.antispider.stream.common.bean.{AnalyzeRule, BookRequestData, ProcessedData, QueryRequestData, RequestType}
import com.air.antispider.stream.common.util.jedis.{JedisConnectionUtil, PropertiesUtil}
import com.air.antispider.stream.common.util.kafka.MyKafkaProducer
import com.air.antispider.stream.dataprocess.businessprocess.{AnalyzeBookRequest, AnalyzeRequest, AnalyzeRuleDB, DataPackage, DataSend, DataSplit, EncryptedData, IpOperation, LinkCount, RequestTypeClassifier, SparkStreamingMonitor, TravelTypeClassifier, URLFilter}
import com.air.antispider.stream.dataprocess.constants.TravelTypeEnum
import com.air.antispider.stream.dataprocess.constants.TravelTypeEnum.TravelTypeEnum
import kafka.serializer.StringDecoder
import org.apache.commons.lang3.StringUtils
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}
import redis.clients.jedis.JedisCluster

import scala.collection.mutable.ArrayBuffer

/**
  * 数据预处理操作的主程序
  */
object DataProcessApp {

  def main(args: Array[String]): Unit = {

    //读取Kafka的数据,进行数据预处理操作

    //1. 构建Spark的运行环境
    val sparkConf: SparkConf = new SparkConf()
      .setAppName("DataProcessApp")
      .setMaster("local[*]")
    //创建SparkContext
    val sc = new SparkContext(sparkConf)

    //构建SparkStreamingContext
    val ssc = new StreamingContext(sc, Seconds(2))

    //查询数据,放入广播变量
    val filterRuleList: ArrayBuffer[String] = AnalyzeRuleDB.getFilterRule()
    //将过滤规则放入广播变量,
    //如果数据发生改变,需要更新广播变量,多线程操作,有可能有安全隐患.
    //@volatile会将修饰的变量放入公共内容,让大家一起使用.
    @volatile var filterRuleBroadcast: Broadcast[ArrayBuffer[String]] = sc.broadcast(filterRuleList)
    //获取标签规则
    val classifyRuleMap: Map[String, ArrayBuffer[String]] = AnalyzeRuleDB.getClassifyRule()
    //将标签规则放入广播变量
    @volatile var classifyRuleBroadcast: Broadcast[Map[String, ArrayBuffer[String]]] = sc.broadcast(classifyRuleMap)

    //获取查询的解析规则
    val queryAnalyzeRule: List[AnalyzeRule] = AnalyzeRuleDB.queryRule(0)
    val bookAnalyzeRule: List[AnalyzeRule] = AnalyzeRuleDB.queryRule(1)
    //将解析规则放入广播变量
    @volatile var queryAnalyzeRuleBroadcast: Broadcast[List[AnalyzeRule]] = sc.broadcast(queryAnalyzeRule)
    @volatile var bookAnalyzeRuleBroadcast: Broadcast[List[AnalyzeRule]] = sc.broadcast(bookAnalyzeRule)
    //获取标签规则
    val blackIPList: ArrayBuffer[String] = AnalyzeRuleDB.queryIpListToBrocast()
    //将标签规则放入广播变量
    @volatile var blackIPBroadcast: Broadcast[ArrayBuffer[String]] = sc.broadcast(blackIPList)

    //将Kafkaproducer放入广播
    val producer: MyKafkaProducer = MyKafkaProducer.getProducer()
    val producerBroadcast: Broadcast[MyKafkaProducer] = sc.broadcast(producer)



    //获取kafka数据
    val inputStream: InputDStream[(String, String)] = getInputStream(ssc)

    //3. 开始进行数据处理
    val sourceDStream: DStream[String] = inputStream.map(_._2)

    //创建Redis连接
    val jedis: JedisCluster = JedisConnectionUtil.getJedisCluster


    sourceDStream.foreachRDD(messageRDD => {
      //更新过滤规则的广播变量
//      1. 先去Redis中取出状态
      var filterRuleChangeStatus: String = jedis.get("filterRuleChangeStatus")
//      "                    " "" null
      if (StringUtils.isBlank(filterRuleChangeStatus)) {
//        2. 判断是否能取出来,如果没有,就先初始化一个
        filterRuleChangeStatus = "true"
        jedis.set("filterRuleChangeStatus", filterRuleChangeStatus)
      }

//      3. 如果有,那么就取出来,看是否为true.
      if (filterRuleChangeStatus.toBoolean) {
//      4. 如果为true,先废除掉之前的广播变量
        filterRuleBroadcast.unpersist()
//      5. 重新查询MySQL,获取最新数据
        val filterRuleList: ArrayBuffer[String] = AnalyzeRuleDB.getFilterRule()
//      6. 将最新数据放入广播变量
        filterRuleBroadcast = sc.broadcast(filterRuleList)
        //7. 更新Redis中的标记为false
        filterRuleChangeStatus = "false"
        jedis.set("filterRuleChangeStatus", filterRuleChangeStatus)
      }

      //标签规则数据更新操作
      //1. 获取redis中的标记
      var classifyRuleChangeStatus: String = jedis.get("classifyRuleChangeStatus")
      //对Redis中的数据进行非空判断
      if (StringUtils.isBlank(classifyRuleChangeStatus)) {
        classifyRuleChangeStatus = "true"
        jedis.set("classifyRuleChangeStatus", classifyRuleChangeStatus)
      }
      //如果数据发生改变,更新广播变量
      if (classifyRuleChangeStatus.toBoolean) {
        //将原来的广播变量废除掉
        classifyRuleBroadcast.unpersist()
        //查询最新的数据
        val classifyRuleMap: Map[String, ArrayBuffer[String]] = AnalyzeRuleDB.getClassifyRule()
        //更新广播变量
        classifyRuleBroadcast = sc.broadcast(classifyRuleMap)
        //更新Redis状态信息
        classifyRuleChangeStatus = "false"
        jedis.set("classifyRuleChangeStatus", classifyRuleChangeStatus)
      }

      //标签规则数据更新操作
      //1. 获取redis中的标记
      var analyzeRuleStatus: String = jedis.get("analyzeRuleStatus")
      //对Redis中的数据进行非空判断
      if (StringUtils.isBlank(analyzeRuleStatus)) {
        analyzeRuleStatus = "true"
        jedis.set("analyzeRuleStatus", analyzeRuleStatus)
      }
      //如果数据发生改变,更新广播变量
      if (analyzeRuleStatus.toBoolean) {
        //将原来的广播变量废除掉
        queryAnalyzeRuleBroadcast.unpersist()
        bookAnalyzeRuleBroadcast.unpersist()
        //查询最新的数据
        val queryAnalyzeRule: List[AnalyzeRule] = AnalyzeRuleDB.queryRule(0)
        val bookAnalyzeRule: List[AnalyzeRule] = AnalyzeRuleDB.queryRule(1)
        //将解析规则放入广播变量
        queryAnalyzeRuleBroadcast = sc.broadcast(queryAnalyzeRule)
        bookAnalyzeRuleBroadcast = sc.broadcast(bookAnalyzeRule)
        //更新广播变量
        classifyRuleBroadcast = sc.broadcast(classifyRuleMap)
        //更新Redis状态信息
        analyzeRuleStatus = "false"
        jedis.set("analyzeRuleStatus", analyzeRuleStatus)
      }

      //标签规则数据更新操作
      //1. 获取redis中的标记
      var blackIPChangeStatus: String = jedis.get("blackIPChangeStatus")
      //对Redis中的数据进行非空判断
      if (StringUtils.isBlank(blackIPChangeStatus)) {
        blackIPChangeStatus = "true"
        jedis.set("blackIPChangeStatus", blackIPChangeStatus)
      }
      //如果数据发生改变,更新广播变量
      if (blackIPChangeStatus.toBoolean) {
        //将原来的广播变量废除掉
        blackIPBroadcast.unpersist()
        //查询最新的数据
        val blackIPList: ArrayBuffer[String] = AnalyzeRuleDB.queryIpListToBrocast()
        //更新广播变量
        blackIPBroadcast = sc.broadcast(blackIPList)
        //更新Redis状态信息
        blackIPChangeStatus = "false"
        jedis.set("blackIPChangeStatus", blackIPChangeStatus)
      }



      //链路统计
      val serversCountMap: collection.Map[String, Int] = LinkCount.linkCount(messageRDD)
      //数据过滤
      val filterMessage: RDD[String] = messageRDD.filter(message => URLFilter.filterURL(message, filterRuleBroadcast.value))
      //手机号加密
      val encryptedPhoneRDD: RDD[String] = EncryptedData.encryptedPhone(filterMessage)



      //数据转换,将string类型的字符转换为结构化数据
      val processedDataRDD: RDD[ProcessedData] = encryptedPhoneRDD.map(message => {
        //身份证号加密
        val encryptedMessage: String = EncryptedData.encryptedID(message)
        //数据切割
        val (request, //请求的URL
        requestMethod, //请求方式
        contentType, //请求的类型
        requestBody, //请求体
        httpReferrer, //来源URL
        remoteAddr, //客户端IP
        httpUserAgent, //UA信息
        timeIso8601, //时间
        serverAddr, //服务器IP
        cookiesStr, //cookie信息
        cookieValue_JSESSIONID, // jssionID信息
        cookieValue_USERID //cookie中的User信息
          ) = DataSplit.split(encryptedMessage)

        //数据打标签
        //如何返回数据,BehaviorTypeEnum操作类型FlightTypeEnum航班类型
        //搞一个类,封装这两个枚举类.进行绑定
        //在ProcessedData类中有一个case class RequestType,里面就封装了操作类型和航班类型
        //请求类型打标签
        val requestType: RequestType = RequestTypeClassifier.classify(request.toString, classifyRuleBroadcast.value)
        //往返信息打标签
        val travelType: TravelTypeEnum=TravelTypeClassifier.travelType(httpReferrer)
        //数据解析
        val queryRequestData: Option[QueryRequestData] = AnalyzeRequest.analyzeQueryRequest(requestType, requestMethod, contentType, request, requestBody, travelType,queryAnalyzeRuleBroadcast.value)
        val bookRequestData: Option[BookRequestData] = AnalyzeBookRequest.analyzeBookRequest(requestType, requestMethod, contentType, request, requestBody, travelType, bookAnalyzeRuleBroadcast.value)
        //数据再次加工,获取高频IP标记
        val highFrqIPGroup: Boolean = IpOperation.isBlackIP(remoteAddr, blackIPBroadcast.value)

        val processedData: ProcessedData = DataPackage.dataPackage(
          "",//我们这里就不传原始数据了,实际开发的时候你根据需要来传递.
          requestMethod,
          request,
          remoteAddr,
          httpUserAgent,
          timeIso8601,
          serverAddr,
          highFrqIPGroup,//高频IP标记
          requestType, //请求类型的标签(国内查询/国际预订)
          travelType, //航班类型
          cookieValue_JSESSIONID,
          cookieValue_USERID,
          queryRequestData, //请求数据的解析参数结果
          bookRequestData,  //预订数据的解析参数结果
          httpReferrer)

        //返回结构化的数据
        processedData
      })
      //进行数据推送,将数据发送给kafka
      DataSend.sendDataToKafka(processedDataRDD, producerBroadcast)

      //Spark状态监控
      SparkStreamingMonitor.streamMonitor(sc, processedDataRDD, serversCountMap)



      processedDataRDD.foreach(println)
    })


    //4. 启动Spark程序
    ssc.start()
    ssc.awaitTermination()

  }

  /**
    * 获取Kafka中的数据,构建InputStream
    * @param ssc
    * @return
    */
  def getInputStream(ssc: StreamingContext):InputDStream[(String, String)] = {
    //2.从Kafka中消费数据
    //消费数据有几种方式?reciver/direct
    //构建Kafka参数
    var kafkaParams = Map[String, String]()
    val brokerList = PropertiesUtil.getStringByKey("default.brokers", "kafkaConfig.properties")
    kafkaParams += ("metadata.broker.list" -> brokerList)

    //构建topic的set
    val topic = PropertiesUtil.getStringByKey("source.nginx.topic", "kafkaConfig.properties")
    val topics = Set[String](topic)
    KafkaUtils.createDirectStream[String, String, StringDecoder,StringDecoder](ssc, kafkaParams, topics)
  }




}
