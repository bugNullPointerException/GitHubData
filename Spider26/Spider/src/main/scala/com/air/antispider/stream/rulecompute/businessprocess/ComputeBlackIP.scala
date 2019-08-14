package com.air.antispider.stream.rulecompute.businessprocess

import com.air.antispider.stream.common.bean.{AntiCalculateResult, FlowCollocation, FlowScoreResult, ProcessedData, RuleCollocation}
import org.apache.spark.rdd.RDD

import scala.collection.mutable.ArrayBuffer

/**
  * 进行黑名单打分
  */
object ComputeBlackIP {


  /**
    * 根据各个规则的计算结果,计算得分
    *
    * @param ipBlockMap
    * @param ipMap
    * @param criticalPagesMap
    * @param uAMap
    * @param criticalPagesMinTimeMap
    * @param criticalPagesTimeCountMap
    * @param flightQueryCountMap
    * @param cookieCountMap
    * @param flowList
    */
  def getBlackIP(processedDataRDD: RDD[ProcessedData],
                 ipBlockMap: collection.Map[String, Int],
                 ipMap: collection.Map[String, Int],
                 criticalPagesMap: collection.Map[String, Int],
                 uAMap: collection.Map[String, Int],
                 criticalPagesMinTimeMap: collection.Map[String, Int],
                 criticalPagesTimeCountMap: collection.Map[String, Int],
                 flightQueryCountMap: collection.Map[String, Int],
                 cookieCountMap: collection.Map[String, Int],
                 flowList: ArrayBuffer[FlowCollocation]):RDD[AntiCalculateResult] = {
    //从map中取出数据
    //通过map转换操作,将ProcessedData转换为AntiCalculateResult
    processedDataRDD.map(line => {
      //取出IP
      val ip: String = line.remoteAddr

      //初始化IP段访问量为0
      var ipBlockCount: Int = 0
      //获取IP段
      val arr: Array[String] = ip.split("\\.")
      if (arr.length == 4) {
        //如果长度不等于4,我们不认为是一个合格的IP
        //构建元组(ip段,1)
        val ipBlock = arr(0) + "." + arr(1)
        //取出IP段数据,如果取不到,给个默认值0
        ipBlockCount = ipBlockMap.getOrElse(ipBlock, 0)
      }
      //取出次数
      val ipCount: Int = ipMap.getOrElse(ip, 0)
      val criticalPagesCount: Int = criticalPagesMap.getOrElse(ip, 0)
      val uaCount: Int = uAMap.getOrElse(ip, 0)
      val criticalPagesMinTimeCount: Int = criticalPagesMinTimeMap.getOrElse(ip, Integer.MAX_VALUE)
      val criticalPagesTimeCount: Int = criticalPagesTimeCountMap.getOrElse(ip, 0)
      val flightQueryCount: Int = flightQueryCountMap.getOrElse(ip, 0)
      val cookieCount: Int = cookieCountMap.getOrElse(ip, 0)

      //创建Map,封装计算结果,为了实现数据绑定.
      val map = Map[String, Int](
        "ip" -> ipCount,
        "criticalPages" -> criticalPagesCount,
        "userAgent" -> uaCount,
        "criticalPagesAccTime" -> criticalPagesMinTimeCount,
        "criticalCookies" -> cookieCount,
        "flightQuery" -> flightQueryCount,
        "criticalPagesLessThanDefault" -> criticalPagesTimeCount,
        "ipBlock" -> ipBlockCount
      )

      //去获取计算结果
      val flowScoreResults: ArrayBuffer[FlowScoreResult] = getComputeBlackIP(map, flowList)

      //封装黑名单计算结果对象
      AntiCalculateResult(line,
        ip,
        ipBlockCount,
        ipCount,
        criticalPagesCount,
        uaCount,
        criticalPagesMinTimeCount,
        criticalPagesTimeCount,
        flightQueryCount,
        cookieCount,
        flowScoreResults.toArray
      )
    })


  }

  /**
    * 开始计算超标数据
    *
    * @param map
    * @param flowList
    */
  def getComputeBlackIP(map: Map[String, Int], flowList: ArrayBuffer[FlowCollocation]): ArrayBuffer[FlowScoreResult] = {
    //创建封装所有流程计算结果的集合
    val resultList = new ArrayBuffer[FlowScoreResult]()
    //1.遍历flowList集合,取出流程
    for (flow <- flowList) {
      //定义该流程的计算结果集合1:超标
      var list1 = new ArrayBuffer[Double]()
      //定义该流程的计算结果集合2:超标并处于开启状态
      var list2 = new ArrayBuffer[Double]()
      //定义一个集合,存储命中的名字
      var hitRules = new ArrayBuffer[String]()

      //2.需要从流程中取出所有的规则List
      val rules: List[RuleCollocation] = flow.rules
      //3.遍历数据库中的规则List,取到每一个规则Rule
      for (rule <- rules) {
        //4.取出Rule的名字,范围,分值,状态信息
        val ruleName: String = rule.ruleName
        //当前规则的范围
        var rangeValue: Double = 0.0
        //如果是criticalPagesLessThanDefault指标,那么取出第二个值
        if (ruleName.equals("criticalPagesLessThanDefault")) {
          rangeValue = rule.ruleValue1
        } else {
          rangeValue = rule.ruleValue0
        }
        //规则的分值
        val score: Int = rule.ruleScore
        //规则的状态
        val status: Int = rule.ruleStatus
        //5.根据rule的名字去map取计算的次数
        //从计算结果的Map中取出的次数
        var num: Int = 0
        //如果当前指标为criticalPagesAccTime,取不到值的时候给个Int最大值
        if (ruleName.equals("criticalPagesAccTime")) {
          num = map.getOrElse(ruleName, Integer.MAX_VALUE)
        } else {
          num = map.getOrElse(ruleName, 0)
        }
        //6.判断次数是否超出的范围
        //使用rangeValue和num进行大小判断
        if (ruleName.equals("criticalPagesAccTime")) {
          if (num < rangeValue) {
            //超标了,放入集合1中
            list1 += score
            //如果命中,把名字记录起来
            hitRules += ruleName
            //7.根据开启状态得到集合2
            //如果状态处于开启状态,将数据放入集合2中
            if (status == 0) {
              list2 += score
            }
          } else {
            //如果没有超出范围,给个0
            list1 += 0
          }
        } else {
          if (num > rangeValue) {
            //超标了,放入集合1中
            list1 += score
            //如果命中,把名字记录起来
            hitRules += ruleName
            //如果状态处于开启状态,将数据放入集合2中
            if (status == 0) {
              list2 += score
            }
          } else {
            //如果没有超出范围,给个0
            list1 += 0
          }
        }
      }
      //8.将两个计算结果集合1,集合2放入算法中,开始计算
      //流程计算得分
      val flowScore: Double = calculateFlowScore(list1.toArray,list2.toArray)
      //是否超出范围
      val isUpLimited = flowScore > flow.flowLimitScore
      //命中时间
      val hitTime = System.currentTimeMillis().toString
      val flowScoreResult = FlowScoreResult(flow.flowId, flowScore, flow.flowLimitScore, isUpLimited, flow.flowId, hitRules.toList, hitTime)
      resultList += flowScoreResult
    }
    //返回所有流程计算结果
    resultList
  }

  /**
    * *
    * 计算流程得分-请参考详细设计说明书（规则打分，流程计算）及对应的原型设计（流 程管理）
    * 系数 2 权重：60%，数据区间：10-60
    * 系数 3 权重：40，数据区间：0-40
    * 系数 2+系数 3 区间为：10-100
    * 系数 1 为:平均分/10
    * 所以，factor1 * (factor2 + factor3)区间为:平均分--10 倍平均分
    *
    * @param scores 超出范围的打分数据
    * @param xa     超出范围,并且开启的打分数据
    * @return 规则得分*/
  def calculateFlowScore(scores: Array[Double], xa: Array[Double]): Double = {
    //总打分
    val sum = scores.sum
    //打分列表长度
    val dim = scores.length
    //系数 1：平均分/10
    val factor1 = sum / (10 * dim)

    //命中规则中，规则分数最高的
    val maxInXa = if (xa.isEmpty) {
      0.0
    } else {
      xa.max
    }
    //系数 2：系数 2 的权重是 60，指的是最高 score 以 6 为分界，最高 score 大于 6，就给满权重 60，不足 6，就给对应的 maxInXa*10
    val factor2 = if (1 < (1.0 / 6.0) * maxInXa) {
      60
    } else {
      (1.0 / 6.0) * maxInXa * 60
    }
    //系数 3：打开的规则总分占总规则总分的百分比，并且系数 3 的权重是 40
    val factor3 = 40 * (xa.sum / sum)

    /**
      * \* 系数 2 权重：60%，数据区间：10-60
      * \* 系数 3 权重：40，数据区间：0-40
      * \* 系数 2+系数 3 区间为：10-100
      * \* 系数 1 为:平均分/10
      * \* 所以，factor1 * (factor2 + factor3)区间为:平均分--10 倍平均分
      */
    factor1 * (factor2 + factor3)

  }


}
