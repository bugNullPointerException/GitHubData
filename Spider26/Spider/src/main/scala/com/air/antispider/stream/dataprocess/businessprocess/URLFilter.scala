package com.air.antispider.stream.dataprocess.businessprocess

import scala.collection.mutable.ArrayBuffer

/**
  * 数据过滤功能
  */
object URLFilter {


  /**
    * 对请求数据进行过滤操作
    * @param message 源数据
    * @param filterRuleList 过滤规则的集合
    * @return
    */
  def filterURL(message: String, filterRuleList: ArrayBuffer[String]): Boolean = {
    //1. 先取出message中的URL信息
//    05/Aug/2019:08:55:50 +0800
//    POST /B2C40/dist/main/css/flight.css HTTP/1.1
    val arr: Array[String] = message.split("#CS#")
    if (arr.length > 1) {
      //如果长度大于1,有URL这一行数据
      val urlArr: Array[String] = arr(1).split(" ")
      if (urlArr.length > 1) {
        //获取URL
        val url: String = urlArr(1)
        //2. 遍历filterRuleList,看是否能匹配上,
        for (filterRule <- filterRuleList) {
          //调用matches进行正则匹配
          if (url.matches(filterRule)){
            //如果匹配上,return false
            return false
          }
        }
        //3.如果整个for循环都走完了,还没有return,那么return true
        return true
      }
    }
    false
  }

}
