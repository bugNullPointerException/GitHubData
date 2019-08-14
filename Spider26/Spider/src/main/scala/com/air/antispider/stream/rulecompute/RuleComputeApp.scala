package com.air.antispider.stream.rulecompute

import java.text.SimpleDateFormat

import com.air.antispider.stream.common.bean.{AntiCalculateResult, FlowCollocation, FlowScoreResult, ProcessedData}
import com.air.antispider.stream.common.util.HDFS.{BlackListToHDFS, BlackListToRedis}
import com.air.antispider.stream.common.util.jedis.{JedisConnectionUtil, PropertiesUtil}
import com.air.antispider.stream.common.util.kafka.KafkaOffsetUtil
import com.air.antispider.stream.dataprocess.businessprocess.AnalyzeRuleDB
import com.air.antispider.stream.rulecompute.businessprocess.{ComputeBlackIP, QueryDataPackage, RuleUtils}
import kafka.common.TopicAndPartition
import kafka.message.MessageAndMetadata
import kafka.serializer.StringDecoder
import org.I0Itec.zkclient.ZkClient
import org.apache.commons.lang3.StringUtils
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{Row, SQLContext}
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import redis.clients.jedis.JedisCluster

import scala.collection.mutable.ArrayBuffer

/**
  * 黑名单实时计算的主程序
  */
object RuleComputeApp {



  def main(args: Array[String]): Unit = {

    //当应用被停止的时候，进行如下设置可以保证当前批次执行完之后再停止应用。
     System.setProperty("spark.streaming.stopGracefullyOnShutdown", "true")
    //获取Spark运行环境
    //1. 构建Spark的运行环境
    val sparkConf: SparkConf = new SparkConf()
      .setAppName("RuleComputeApp")
      .setMaster("local[*]")
    //创建SparkContext
    val sc = new SparkContext(sparkConf)
    val sqlContext = new SQLContext(sc)
    //构建SparkStreamingContext
    val ssc = new StreamingContext(sc, Seconds(2))
    //查询关键页面数据,放入广播变量
    val criticalPagesList: ArrayBuffer[String] = AnalyzeRuleDB.queryCriticalPages()
    @volatile var criticalPagesBroadcast: Broadcast[ArrayBuffer[String]] = sc.broadcast(criticalPagesList)
    //获取"查询"流程数据,放入广播变量
    val flowList: ArrayBuffer[FlowCollocation] = AnalyzeRuleDB.createFlow(0)
    @volatile var flowBroadcast: Broadcast[ArrayBuffer[FlowCollocation]] = sc.broadcast(flowList)


    //zk信息
    val zkHosts = PropertiesUtil.getStringByKey("zkHosts", "zookeeperConfig.properties")
    val zkClient = new ZkClient(zkHosts)
    val zkPath = PropertiesUtil.getStringByKey("rulecompute.antispider.zkPath", "zookeeperConfig.properties")
    //自定义维护offset
    val inpuStream:InputDStream[(String, String)] = getCustumerKafkaMessage(ssc, zkClient, zkHosts, zkPath)

    //取出格式化数据
    val messageDStream: DStream[String] = inpuStream.map(_._2)

    //将数据进行格式化操作
    val processedDataDStream: DStream[ProcessedData] = QueryDataPackage.queryDataLoadAndPackage(messageDStream)
    //创建Redis连接
    val jedis: JedisCluster = JedisConnectionUtil.getJedisCluster


    processedDataDStream.foreachRDD(processedDataRDD => {
      //更新广播变量
      //关键页面列表数据更新操作
      //1. 获取redis中的标记
      var criticalPagesStatus: String = jedis.get("criticalPagesStatus")
      //对Redis中的数据进行非空判断
      if (StringUtils.isBlank(criticalPagesStatus)) {
        criticalPagesStatus = "true"
        jedis.set("criticalPagesStatus", criticalPagesStatus)
      }
      //如果数据发生改变,更新广播变量
      if (criticalPagesStatus.toBoolean) {
        //将原来的广播变量废除掉
        criticalPagesBroadcast.unpersist()
        //查询最新的数据
        val criticalPagesList: ArrayBuffer[String] = AnalyzeRuleDB.queryCriticalPages()
        //更新广播变量
        criticalPagesBroadcast = sc.broadcast(criticalPagesList)
        //更新Redis状态信息
        criticalPagesStatus = "false"
        jedis.set("criticalPagesStatus", criticalPagesStatus)
      }

      //流程信息数据更新操作
      //1. 获取redis中的标记
      var flowStatus: String = jedis.get("flowStatus")
      //对Redis中的数据进行非空判断
      if (StringUtils.isBlank(flowStatus)) {
        flowStatus = "true"
        jedis.set("flowStatus", flowStatus)
      }
      //如果数据发生改变,更新广播变量
      if (flowStatus.toBoolean) {
        //将原来的广播变量废除掉
        flowBroadcast.unpersist()
        //查询最新的数据
        val flowList: ArrayBuffer[FlowCollocation] = AnalyzeRuleDB.createFlow(0)
        //更新广播变量
        flowBroadcast = sc.broadcast(flowList)
        //更新Redis状态信息
        flowStatus = "false"
        jedis.set("flowStatus", flowStatus)
      }
      //规则计算
      //每个指标的维度都是5分钟,我们使用2秒来计算
      val ipBlockMap: collection.Map[String, Int] = RuleUtils.getIpBlockCount(processedDataRDD)
      //统计IP5分钟的访问量
      val ipMap: collection.Map[String, Int] = RuleUtils.getIpCount(processedDataRDD)
      //统计IP5分钟关键页面的访问量
      val criticalPagesMap: collection.Map[String, Int] = RuleUtils.getcriticalPagesCount(processedDataRDD, criticalPagesBroadcast.value)
      //统计IP5分钟携带不同UA的个数
      val uAMap: collection.Map[String, Int] = RuleUtils.getUACount(processedDataRDD)
      //统计IP5分钟访问关键页面最小时间间隔
      val criticalPagesMinTimeMap: collection.Map[String, Int] = RuleUtils.getcriticalPagesMinTime(processedDataRDD, criticalPagesBroadcast.value)
      //统计IP5分钟访问关键页面小于预设时间的次数
      val criticalPagesTimeCountMap: collection.Map[String, Int] = RuleUtils.getcriticalPagesTimeCount(processedDataRDD, criticalPagesBroadcast.value)
      //统计IP5分钟查询不同行程的次数
      val flightQueryCountMap: collection.Map[String, Int] = RuleUtils.getflightQueryCount(processedDataRDD)
      //统计IP5分钟携带不同Cookie的次数
      val cookieCountMap: collection.Map[String, Int] = RuleUtils.getcookieCount(processedDataRDD)

      //黑名单计算业务
      val antiCalculateResultRDD: RDD[AntiCalculateResult] = ComputeBlackIP.getBlackIP(
        processedDataRDD,
        ipBlockMap,
        ipMap,
        criticalPagesMap,
        uAMap,
        criticalPagesMinTimeMap,
        criticalPagesTimeCountMap,
        flightQueryCountMap,
        cookieCountMap,
        flowBroadcast.value
      )
      //将没有超出范围的数据过滤掉
      val blackIPResultRdd: RDD[AntiCalculateResult] = antiCalculateResultRDD.filter(antiCalculateResult => {
        //从AntiCalculateResult取出打分数据,看有没有超出范围的,如果有就要,如果没有,就不要
        //flowsScore对应的是多个流程的计算结果.
        val results: Array[FlowScoreResult] = antiCalculateResult.flowsScore.filter(flowScore => {
          //如果超标isUpLimited为true,
          flowScore.isUpLimited
        })
        //如果上面的集合经过过滤之后,里面还有数据,集合不为空,那么就返回true
        results.nonEmpty
      })
      //过滤之后的数据,将数据转换为(ip, 打分结果)
      val resultRDD: RDD[(String, FlowScoreResult)] = blackIPResultRdd.map(antiCalculateResult => {
        //返回(ip, 打分的最终结果数据)
        (antiCalculateResult.ip, antiCalculateResult.flowsScore.last)
      })
        //按照IP进行聚合操作,返回值因为是x还是y都一样.
        .reduceByKey((x, y) => y)


      //存入Redis之前先检查Redis中数据的状态,如果Redis需要进行数据恢复,那么就先恢复数据
      BlackListToRedis.blackListDataToRedis(jedis, sc, sqlContext)

      //将数据存入Redis
      //map转换,将(ip,flowscore) => Row
      val rowRDD: RDD[Row] = resultRDD.map(line => {
        val ip: String = line._1
        val flowScore: FlowScoreResult = line._2
        //创建Redis连接
        val cluster: JedisCluster = JedisConnectionUtil.getJedisCluster
        //构建key   CSANTI_ANTI_BLACK:192.168.80.81:流程ID
        val blackListKey = PropertiesUtil
          .getStringByKey("cluster.key.anti_black_list", "jedisConfig.properties") + s":${ip}:${flowScore.flowId}"
        //redis 黑名单库中的值：flowScore|strategyCode|hitRules|time
        val blackListValue = s"${flowScore.flowScore}|${flowScore.flowStrategyCode}|${flowScore.hitRules.mkString(",")}|${flowScore.hitTime}"
        val ex = PropertiesUtil
          .getStringByKey("cluster.exptime.monitor", "jedisConfig.properties").toInt
        //存入Redis
        cluster.setex(blackListKey, ex, blackListValue)
        Row(ex.toString, blackListKey, blackListValue)
      })

      //在存入redis之后进行备份到HDFS
      BlackListToHDFS.saveAntiBlackList(rowRDD ,sqlContext)





      antiCalculateResultRDD.foreach(println)

      println("IP段的2秒钟访问量:" + ipBlockMap)
      println("IP2秒钟访问量:" + ipMap)
      println("IP2秒钟关键页面的访问量:" + criticalPagesMap)
      println("IP2秒钟携带不同UA的个数:" + uAMap)
      println("统计IP5分钟访问关键页面最小时间间隔:" + criticalPagesMinTimeMap)
      println("统计IP5分钟访问关键页面小于预设时间的次数:" + criticalPagesTimeCountMap)
      println("统计IP5分钟查询不同行程的次数:" + flightQueryCountMap)
      println("统计IP5分钟携带不同Cookie的次数:" + cookieCountMap)

    })






    //黑名单计算

    //恢复Redis中的黑名单数据

    //将黑名单数据存入Redis

    //将结构化的数据存入HDFS

    /*

    *存储规则计算结果 RDD（antiCalculateResults） 到 HDFS

    */

    messageDStream.foreachRDD(line =>{
      //构建日期
      val date = new SimpleDateFormat("yyyy/MM/dd/HH").format(System.currentTimeMillis())
      //构建路径
      val yyyyMMddHH = date.replace("/","").toInt //2019080916
      //        /csair/data/rule-black-list/itheima/2019080915/part-00000
      val path: String = PropertiesUtil.getStringByKey("blackListPath","HDFSPathConfig.properties")+"itheima/"+yyyyMMddHH
      try{
        sc
          .textFile(path+"/part-00000")
          //合并,将本次数据和以前的数据进行合并
          .union(line)
          //重分区,合并为1个文件
          .repartition(1)
          .saveAsTextFile(path)
      }catch{
        case e: Exception =>
          //如果发生异常,直接存储
          line.repartition(1).saveAsTextFile(path)
      } })



    //将最新的偏移量更新到zookeeper中
    inpuStream.foreachRDD(rdd => {
      KafkaOffsetUtil.saveOffsets(zkClient, zkHosts, zkPath,rdd)
    })

    //执行程序
    ssc.start()
    ssc.awaitTermination()
  }


  def getCustumerKafkaMessage(ssc: StreamingContext, zkClient: ZkClient, zkHosts: String, zkPath: String): InputDStream[(String, String)] = {
    //Kafka参数
    var kafkaParams = Map[String, String]()
    val brokerList = PropertiesUtil.getStringByKey("default.brokers", "kafkaConfig.properties")
    kafkaParams += ("metadata.broker.list" -> brokerList)

    //Topic信息
    val topic = PropertiesUtil.getStringByKey("source.query.topic", "kafkaConfig.properties")
    val topics = Set[String](topic)

    //调用工具类,根据zk信息和topic信息获取Map[TopicAndPartition, Long]
    val topicAndPartitionOption: Option[Map[TopicAndPartition, Long]] = KafkaOffsetUtil.readOffsets(zkClient, zkHosts, zkPath, topic)

    //看option里面是否有数据
    topicAndPartitionOption match {
      case Some(fromOffsets) => {
        //如果有偏移量信息,那么就自己来维护
        //消费Kafka数据
        val handler = (metaData: MessageAndMetadata[String, String]) => (metaData.key, metaData.message)
        KafkaUtils.createDirectStream[String, String, StringDecoder,StringDecoder,(String, String)](ssc, kafkaParams, fromOffsets, handler)
      }

      case None =>
        //消费Kafka数据
        KafkaUtils.createDirectStream[String, String, StringDecoder,StringDecoder](ssc, kafkaParams, topics)
    }
  }
}
