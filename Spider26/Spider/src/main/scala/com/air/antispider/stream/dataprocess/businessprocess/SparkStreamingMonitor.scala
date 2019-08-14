package com.air.antispider.stream.dataprocess.businessprocess

import com.air.antispider.stream.common.bean.ProcessedData
import com.air.antispider.stream.common.util.jedis.{JedisConnectionUtil, PropertiesUtil}
import com.air.antispider.stream.common.util.spark.SparkMetricsUtils
import com.alibaba.fastjson.JSONObject
import org.apache.commons.lang3.time.FastDateFormat
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.json4s.DefaultFormats
import org.json4s.jackson.Json
import redis.clients.jedis.JedisCluster

/**
  * Spark状态监控
  *
  */
object SparkStreamingMonitor {
  /**
    * 数据预处理状态监控
    * @param processedDataRDD
    * @param serversCountMap
    */
  def streamMonitor(sc: SparkContext, processedDataRDD: RDD[ProcessedData], serversCountMap: collection.Map[String, Int]) = {
    //获取包含时间差的JSON
    //httpclient => http://localhost:4040/metrics/json/

    //监控数据获取
    //val sparkDriverHost = sc.getConf.get("spark.org.apache.hadoop.yarn.server.webproxy.amfilter.AmIpFilter.param.PROXY _URI_BASES")
    //在 yarn 上运行的监控数据 json 路径
    //val url = s"${sparkDriverHost}/metrics/json"

    val url = "http://localhost:4040/metrics/json/"
    val metricsObj: JSONObject = SparkMetricsUtils.getMetricsJson(url)
    //解析数据
    //先获取gauges对象
    val gaugesObj: JSONObject = metricsObj.getJSONObject("gauges")
    //拼接开始和结束时间的key
    val applicationId: String = sc.applicationId //local-15345345353
    val appName: String = sc.appName
    //开始时间的key
    val startTimeKey = applicationId + ".driver." + appName + ".StreamingMetrics.streaming.lastCompletedBatch_processingStartTime"
    //结束时间的key
    val endTimeKey = applicationId + ".driver." + appName + ".StreamingMetrics.streaming.lastCompletedBatch_processingEndTime"
    //获取开始和结束时间
    val startTime = gaugesObj.getJSONObject(startTimeKey).getLong("value")
    val endTime = gaugesObj.getJSONObject(endTimeKey).getLong("value")
    //将endTime格式化为yyyy-MM-dd HH:mm:ss格式
    val endTimeStr: String = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss").format(endTime)




    //获取时间差,开始计算
    val costTime = endTime - startTime
    //获取数量
    val count: Long = processedDataRDD.count()

    //计算速度 数量/时间差
    var countPerMillis:Long = 0
    if (costTime != 0) {
      //如果时间差不等于0 ,那么就开始计算,
      countPerMillis = count / costTime
    }
    //将数据存入Redis,供前端使用
    //构建一个map来封装数据,
    val map: Map[String, Any] = Map[String, Any](
      "costTime" -> costTime,
      "serversCountMap" -> serversCountMap,
      "applicationId" -> applicationId,
      "countPerMillis" -> countPerMillis,
      "applicationUniqueName" -> appName,
      "sourceCount" -> count,
      "endTime" -> endTimeStr
    )
    //将map转换为json
    val json: String = Json(DefaultFormats).write(map)

    //将json存入redis
    //创建Redis连接
    val jedis: JedisCluster = JedisConnectionUtil.getJedisCluster
    val key: String = PropertiesUtil.getStringByKey("cluster.key.monitor.dataProcess", "jedisConfig.properties") + System.currentTimeMillis()
    val ex: Int = PropertiesUtil.getStringByKey("cluster.exptime.monitor", "jedisConfig.properties").toInt
    //存入数据
    jedis.setex(key, ex, json)
  }

}
