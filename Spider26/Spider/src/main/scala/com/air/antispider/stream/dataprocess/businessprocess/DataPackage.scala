package com.air.antispider.stream.dataprocess.businessprocess

import com.air.antispider.stream.common.bean.{BookRequestData, CoreRequestParams, ProcessedData, QueryRequestData, RequestType}
import com.air.antispider.stream.dataprocess.constants.TravelTypeEnum.TravelTypeEnum

/**
  * 数据结构化操作
  */
object DataPackage {
  def dataPackage(sourceData: String,
                  requestMethod: String,
                  request: String,
                  remoteAddr: String,
                  httpUserAgent: String,
                  timeIso8601: String,
                  serverAddr: String,
                  highFrqIPGroup: Boolean,
                  requestType: RequestType,
                  travelType: TravelTypeEnum,
                  cookieValue_JSESSIONID: String,
                  cookieValue_USERID: String,
                  queryRequestData: Option[QueryRequestData],
                  bookRequestData: Option[BookRequestData],
                  httpReferrer: String): ProcessedData = {
    /*val map = Map[String, String](
      "useranme" -> "zhangsan"
    )
    val username: Option[String] = map.get("username")

    username match {
      case Some(x) => println(x)
      case None =>
    }*/

    //从解析数据中取出核心请求参数
    //飞行日期
    var flightDate: String = ""
    //出发地
    var depcity: String = ""
    //目的地
    var arrcity: String = ""

    queryRequestData match {
      case Some(x) => {
        flightDate = x.flightDate
        depcity = x.depCity
        arrcity = x.arrCity
      }
      case None => {
        //如果从查询请求参数中没有取到数据
        //那么取预订里面看看有没有数据
        bookRequestData match {
          case Some(y) => {
            flightDate = y.flightDate.mkString(",")
            depcity = y.depCity.mkString(",")
            arrcity = y.arrCity.mkString(",")
          }
          case None =>
            //如果两个里面都没有值,那就不管了.
        }
      }
    }
    //构建核心请求参数
    val requestParams = CoreRequestParams(flightDate, depcity, arrcity)

    ProcessedData(sourceData,
      requestMethod,
      request,
      remoteAddr,
      httpUserAgent,
      timeIso8601,
      serverAddr,
      highFrqIPGroup,
      requestType,
      travelType,
      requestParams,
      cookieValue_JSESSIONID,
      cookieValue_USERID,
      queryRequestData,
      bookRequestData,
      httpReferrer
    )
  }

}
