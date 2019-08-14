package com.air.antispider.stream.dataprocess.businessprocess

import com.air.antispider.stream.common.bean.RequestType
import com.air.antispider.stream.dataprocess.constants.BehaviorTypeEnum.BehaviorTypeEnum
import com.air.antispider.stream.dataprocess.constants.{BehaviorTypeEnum, FlightTypeEnum}

import scala.collection.mutable.ArrayBuffer

/**
  * 数据打标签操作
  */
object RequestTypeClassifier {


  /**
    * 根据传入的URL和规则进行打标签操作
    * @param url
    * @param classifyRuleMap
    * @return
    */
  def classify(url: String, classifyRuleMap: Map[String, ArrayBuffer[String]]): RequestType = {
    //map中包含4个List集合
    //将map中的集合取出来
    //国内查询
    val nationalQueryList: ArrayBuffer[String] = classifyRuleMap.getOrElse("nationalQueryList", null)
    //国内预订
    val nationalBookList: ArrayBuffer[String] = classifyRuleMap.getOrElse("nationalBookList", null)
    //国际查询
    val internationalQueryList: ArrayBuffer[String] = classifyRuleMap.getOrElse("internationalQueryList", null)
    //国际预订
    val internationalBookList: ArrayBuffer[String] = classifyRuleMap.getOrElse("internationalBookList", null)
    //开始进行比对
    for (classifyRule <- nationalQueryList) {
      //用URL和规则比对
      if (url.matches(classifyRule)) {
        //如果比对上,那就看当前集合属于哪一种标签,
        return RequestType(FlightTypeEnum.National, BehaviorTypeEnum.Query)
      }
    }
    //下一个上
    for (classifyRule <- nationalBookList) {
      //用URL和规则比对
      if (url.matches(classifyRule)) {
        //如果比对上,那就看当前集合属于哪一种标签,
        return RequestType(FlightTypeEnum.National, BehaviorTypeEnum.Book)
      }
    }
    //下一个上
    for (classifyRule <- internationalQueryList) {
      //用URL和规则比对
      if (url.matches(classifyRule)) {
        //如果比对上,那就看当前集合属于哪一种标签,
        return RequestType(FlightTypeEnum.International, BehaviorTypeEnum.Query)
      }
    }
    //下一个上
    for (classifyRule <- internationalBookList) {
      //用URL和规则比对
      if (url.matches(classifyRule)) {
        //如果比对上,那就看当前集合属于哪一种标签,
        return RequestType(FlightTypeEnum.International, BehaviorTypeEnum.Book)
      }
    }
    //如果全部都没匹配上,打一个找不到的标签.

    RequestType(FlightTypeEnum.Other, BehaviorTypeEnum.Other)
  }

}
