package com.air.antispider.stream.rulecompute.businessprocess

import java.util
import java.util.Date

import com.air.antispider.stream.common.bean.ProcessedData
import org.apache.commons.lang3.time.FastDateFormat
import org.apache.spark.rdd.RDD

import scala.collection.mutable.ArrayBuffer

/**
  * 规则计算的工具类
  */
object RuleUtils {
  /**
    * 统计IP5分钟携带不同Cookie的次数
    * @param processedDataRDD
    * @return
    */
  def getcookieCount(processedDataRDD: RDD[ProcessedData]): collection.Map[String, Int] = {
    //先数据转换 ProcessedData => (ip, sessionID)
    processedDataRDD.map(line => {
      //取数据
      val ip: String = line.remoteAddr
      val sessionID: String = line.cookieValue_JSESSIONID
      (ip, sessionID)
    })
    //数据分组
      .groupByKey()
    //将(ip, List(sessionID)) => (ip, 不同sessionID的次数)
      .map(line => {
      //获取数据
      val ip: String = line._1
      val sessionList: Iterable[String] = line._2
      //创建set集合
      var set = Set[String]()
      //遍历集合,放入Set进行去重操作
      for (sessionID <- sessionList) {
        set += sessionID
      }
      //返回(ip,不同行程的次数)
      (ip, set.size)
    })
    //数据采集
      .collectAsMap()
  }

  /**
    * 统计IP5分钟查询不同行程的次数
    *
    * @param processedDataRDD
    * @return
    */
  def getflightQueryCount(processedDataRDD: RDD[ProcessedData]): collection.Map[String, Int] = {
    //1. 先进行数据转换ProcessedData => (IP,出发地->目的地)
    processedDataRDD.map(line => {
      val ip: String = line.remoteAddr
      //取出出发地和目的地
      val depcity: String = line.requestParams.depcity
      val arrcity: String = line.requestParams.arrcity
      (ip, depcity + "->" + arrcity)
    })
    //2. 分组操作
      .groupByKey()
    //3. 将集合转换为次数 (IP,List) => (IP,次数)
      .map(line => {
      //获取数据
      val ip: String = line._1
      val cityList: Iterable[String] = line._2
      //创建set集合
      var set = Set[String]()
      //遍历集合,放入Set进行去重操作
      for (city <- cityList) {
        set += city
      }
      //返回(ip,不同行程的次数)
      (ip, set.size)
    })
    //4. 采集数据
      .collectAsMap()
  }

  /**
    * 统计IP5分钟访问关键页面小于预设时间的次数
    *
    * @param processedDataRDD
    * @param criticalPagesList
    * @return
    */
  def getcriticalPagesTimeCount(processedDataRDD: RDD[ProcessedData], criticalPagesList: ArrayBuffer[String]): collection.Map[String, Int] = {
    //过滤,将非关键页面过滤掉
    processedDataRDD.filter(line => {
      //取出URL
      val url: String = line.request
      var flag = 0
      //判断当前的请求是否是一个关键页面
      for (pageURL <- criticalPagesList) {
        if (url.matches(pageURL)) {
          flag = 1
        }
      }
      //如果是关键页面,返回true,我想要这个数据
      if (flag == 1) {
        true
      } else {
        false
      }
    })
      //数据转换将ProcessedData => (ip,时间)
      .map(line => {
      val ip: String = line.remoteAddr
      //2019-08-06T15:34:21+08:00
      val timeStr: String = line.timeIso8601
      //将数据转换为Long类型
      //'T',1:要么对原字符串直接replace替换,2: 使用'T'代表固定的字符串
      val time: Long = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss").parse(timeStr).getTime
      //返回(ip,时间戳)
      (ip, time)
    })
      //分组操作
      .groupByKey()
      //数据转换(ip,时间集合) => (ip,次数)
      .map(line => {
      //1. 获取数据ip/集合
      val ip: String = line._1
      val timeList: Iterable[Long] = line._2
      //2. 将时间进行排序
      val timeArray: Array[Long] = timeList.toArray
      util.Arrays.sort(timeArray)
      //定义一个计数器
      var count = 0
      //3.  求时间差
      for (i <- 0 until timeArray.length - 1) {
        //怎么求差值?两两之间求差值
        val currentTime = timeArray(i)
        val nextTime = timeArray(i + 1)
        //3.1 使用时间差和最小访问时间做大小判断
        val result = nextTime - currentTime
        //写死的一个最小访问时间间隔,正常情况下,应该从Mysql数据库中读取此变量
        val minTime = 5000
        if (result < minTime) {
          //3.2 根据判断结果开始计数
          count = count + 1
        }
      }
      //4. 返回计数结果
      (ip, count)
    })
    //采集数据
      .collectAsMap()
  }


  /**
    * 统计IP5分钟访问关键页面最小时间间隔
    *
    * @param processedDataRDD
    * @param criticalPagesList
    * @return
    */
  def getcriticalPagesMinTime(processedDataRDD: RDD[ProcessedData], criticalPagesList: ArrayBuffer[String]): collection.Map[String, Int] = {
    //1.先获取关键页面,过滤
    processedDataRDD.filter(line => {
      //取出URL
      val url: String = line.request
      var flag = 0
      //判断当前的请求是否是一个关键页面
      for (pageURL <- criticalPagesList) {
        if (url.matches(pageURL)) {
          flag = 1
        }
      }
      //如果是关键页面,返回true,我想要这个数据
      if (flag == 1) {
        true
      } else {
        false
      }
    })
      //将数据转换为元组(ip,时间)
      .map(line => {
      val ip: String = line.remoteAddr
      //2019-08-06T15:34:21+08:00
      val timeStr: String = line.timeIso8601
      //将数据转换为Long类型
      //'T',1:要么对原字符串直接replace替换,2: 使用'T'代表固定的字符串
      val time: Long = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss").parse(timeStr).getTime
      //返回(ip,时间戳)
      (ip, time)
    })
      //分组操作
      .groupByKey()
      //转换操作(ip, 时间的集合)  => (ip,最小访问时间)
      .map(line => {
      //定义最终的时间差
      var minTime = Integer.MAX_VALUE
      //取出原来的元组中的数据
      val ip: String = line._1
      val timeList: Iterable[Long] = line._2
      //对原始集合timeList进行排序
      val timeArray: Array[Long] = timeList.toArray
      if (timeArray.length > 1) {
        //如果原始的时间集合数量大于1,才能进行排序操作
        //对原始的时间进行排序操作
        util.Arrays.sort(timeArray)

        //创建set集合,存放时间差
        var set = Set[Long]()
        //开始求时间差
        for (i <- 0 until timeArray.length - 1) {
          //怎么求差值?两两之间求差值
          val currentTime = timeArray(i)
          val nextTime = timeArray(i + 1)
          val result = nextTime - currentTime
          //将时间差放入set集合
          set += result
        }
        val setArray: Array[Long] = set.toArray
        //对时间差进行排序
        util.Arrays.sort(setArray)
        //取出时间差的第一位
        //如果时间差集合有值,取出第一个
        minTime = setArray(0).toInt
      }
      (ip, minTime)
    })
      //数据采集
      .collectAsMap()
  }

  /**
    * 统计IP5分钟携带不同UA的个数
    *
    * @param processedDataRDD
    * @return
    */
  def getUACount(processedDataRDD: RDD[ProcessedData]): collection.Map[String, Int] = {

    //1. 将数据转换为(IP,UA)
    val groupRDD: RDD[(String, Iterable[String])] = processedDataRDD.map(line => {
      val ip: String = line.remoteAddr
      val ua: String = line.httpUserAgent
      (ip, ua)
    })
      .groupByKey()
    //将(String, Iterable[String]) => (IP, 3)
    groupRDD.map(line => {
      val ip: String = line._1
      val uaList: Iterable[String] = line._2
      //创建Set集合,存放ua信息,进行去重操作
      var set = Set[String]()
      //遍历集合,将数据放入set里面
      for (ua <- uaList) {
        set += ua
      }
      //返回ip和set的长度(不同ua的个数)
      (ip, set.size)
    })
      //采集数据
      .collectAsMap()
  }


  /**
    * 统计IP5分钟关键页面的访问量
    *
    * @param processedDataRDD
    * @return
    */
  def getcriticalPagesCount(processedDataRDD: RDD[ProcessedData], criticalPagesList: ArrayBuffer[String]): collection.Map[String, Int] = {

    //1. 如果是关键页面,将数据转换为(ip, 1)
    processedDataRDD.map(line => {
      //取出数据
      val url: String = line.request
      val ip: String = line.remoteAddr
      //1.1 判断是否是关键页面
      var flag = 0
      for (pageURL <- criticalPagesList) {
        //^.*/B2C40/query/jaxb/direct/query.ao.*$
        if (url.matches(pageURL)) {
          //1.2 如果是关键页面,计数为1
          flag = 1
        }
      }
      //如果是关键页面,返回1次.否则返回0次
      if (flag == 1) {
        (ip, 1)
      } else {
        (ip, 0)
      }
    })
      //2. 累加数据
      .reduceByKey(_ + _)
      //3. 采集数据
      .collectAsMap()
  }


  /**
    * 计算IP访问量
    *
    * @param processedDataRDD
    * @return
    */
  def getIpCount(processedDataRDD: RDD[ProcessedData]): collection.Map[String, Int] = {
    //先将ProcessedData => (ip, 1)
    processedDataRDD.map(line => {
      val ip = line.remoteAddr
      (ip, 1)
    })
      //进行累加
      .reduceByKey(_ + _)
      //采集,转换为Map
      .collectAsMap()
  }

  /**
    * 计算IP段的访问量
    *
    * @param processedDataRDD
    */
  def getIpBlockCount(processedDataRDD: RDD[ProcessedData]): collection.Map[String, Int] = {
    //ProcessedData => (ip段,1)
    val mapRDD: RDD[(String, Int)] = processedDataRDD.map(processedData => {
      //客户端IP
      val ip: String = processedData.remoteAddr
      //获取ip前2位
      //切割
      //substring
      val arr: Array[String] = ip.split("\\.")
      if (arr.length == 4) {
        //如果长度不等于4,我们不认为是一个合格的IP
        //构建元组(ip段,1)
        val ipBlock = arr(0) + "." + arr(1)
        (ipBlock, 1)
      } else {
        //如果ip取不到,那么返回空
        ("", 1)
      }
    })
    //累加
    val reduceRDD: RDD[(String, Int)] = mapRDD.reduceByKey(_ + _)
    //{"192.168": 34}
    reduceRDD.collectAsMap()
  }

}
