package com.air.antispider.stream.common.bean

import com.air.antispider.stream.dataprocess.constants.BehaviorTypeEnum.BehaviorTypeEnum
import com.air.antispider.stream.dataprocess.constants.FlightTypeEnum.FlightTypeEnum
import com.air.antispider.stream.dataprocess.constants.TravelTypeEnum.TravelTypeEnum
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

/**
  * 保存请求参数的结构化数据
  * sourceData：请求原始数据
  * requestMethod：请求方法
  * request：请求路径
  * remoteAddr：客户端ip
  * httpUserAgent：代理
  * timeIso8601：时间
  * serverAddr：请求的服务器地址
  * criticalCookie：cookie信息
  * highFrqIPGroup：此次请求中的ip地址是否命中高频ip
  * requestType：请求类型
  * travelType：往返类型
  * requestParams：核心请求参数，飞行时间、目的地、出发地 ===================
  * cookieValue_JSESSIONID：cookie中的jessionid
  * cookieValue_USERID：cookie中的userid
  * queryRequestData：查询请求的form数据 查询的时候所有的请求参数
  * bookRequestData：预定请求的body数据  预订的时候所有的请求参数
  * httpReferrer：refer
  */
//保存结构化数据
case class ProcessedData(sourceData: String, requestMethod: String, request: String,
                         remoteAddr: String, httpUserAgent: String, timeIso8601: String,
                         serverAddr: String,  highFrqIPGroup: Boolean,
                         requestType: RequestType, travelType: TravelTypeEnum,
                         requestParams: CoreRequestParams, cookieValue_JSESSIONID: String, cookieValue_USERID: String,
                         queryRequestData: Option[QueryRequestData], bookRequestData: Option[BookRequestData],
                         httpReferrer: String) {

  //用null替换空数据
  implicit class StringUtils(s: String) {
    def repEmptyStr(replacement: String = "NULL"): String = {
      if (s.isEmpty) replacement else s
    }
  }

  //推送到kafka的数据格式，使用#CS#分隔数据
  def toKafkaString(fieldSeparator: String = "#CS#"): String = {

    //转换查询参数和预订参数对象为JSON
    val mapper = new ObjectMapper();
    mapper.registerModule(DefaultScalaModule)
    val queryRequestDataStr = queryRequestData match {
      case Some(value) =>
        try {
          mapper.writeValueAsString(value)
        } catch {
          case _: Throwable => ""
        }
      case _ => ""
    }
    val bookRequestDataStr = bookRequestData match {
      case Some(value) =>
        try {
          mapper.writeValueAsString(value)
        } catch {
          case _: Throwable => ""
        }
      case _ => ""
    }

    //_0 - 原始数据
    sourceData.repEmptyStr() + fieldSeparator +
      //_1 - 请求类型 GET/POST
      requestMethod.repEmptyStr() + fieldSeparator +
      //_2 - 请求 http://xxxxx
      request.repEmptyStr() + fieldSeparator +
      //_3 - 客户端地址(IP)
      remoteAddr.repEmptyStr() + fieldSeparator +
      //_4 - 客户端浏览器(UA)
      httpUserAgent.repEmptyStr() + fieldSeparator +
      //_5 - 服务器时间的ISO 8610格式
      timeIso8601.repEmptyStr() + fieldSeparator +
      //_6 - 服务器端地址
      serverAddr.repEmptyStr() + fieldSeparator +
      //_8 - 是否属于高频IP段
      highFrqIPGroup + fieldSeparator +
      //_9 - 航班类型-National/International/Other
      requestType.flightType + fieldSeparator +
      //_10 - 请求行为-Query/Book/Other
      requestType.behaviorType + fieldSeparator +
      //_11 - 行程类型-OneWay/RoundTrip/Unknown
      travelType + fieldSeparator +
      //_12 - 航班日期 -
      requestParams.flightDate.repEmptyStr() + fieldSeparator +
      //_13 - 始发地 -
      requestParams.depcity.repEmptyStr() + fieldSeparator +
      //_14 - 目的地 -
      requestParams.arrcity.repEmptyStr() + fieldSeparator +
      //_15 - 关键Cookie - JSESSIONID
      cookieValue_JSESSIONID.repEmptyStr() + fieldSeparator +
      //_16 - 关键Cookie - 用户ID
      cookieValue_USERID.repEmptyStr() + fieldSeparator +
      //_17 - 解析的查询参数对象JSON
      queryRequestDataStr.repEmptyStr() + fieldSeparator +
      //_18 - 解析的购票参数对象JSON
      bookRequestDataStr.repEmptyStr() + fieldSeparator +
      //_19 - 当前请求是从哪个请求跳转过来的
      httpReferrer.repEmptyStr()

  }
}

//封装请求类型：航线类别（ 0-国内，1-国际，-1-其他）  和 操作类别 （0-查询，1-预定，-1-其他）
case class RequestType(flightType: FlightTypeEnum, behaviorType: BehaviorTypeEnum)

//用于封装核心请求信息：飞行时间、目的地、出发地
case class CoreRequestParams(flightDate: String, depcity: String, arrcity: String)

