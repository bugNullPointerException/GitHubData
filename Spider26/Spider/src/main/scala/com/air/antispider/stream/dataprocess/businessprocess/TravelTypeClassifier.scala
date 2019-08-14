package com.air.antispider.stream.dataprocess.businessprocess

import java.util.regex.{Matcher, Pattern}

import com.air.antispider.stream.dataprocess.constants.TravelTypeEnum
import com.air.antispider.stream.dataprocess.constants.TravelTypeEnum.TravelTypeEnum

/**
  * 对请求的往返信息打标签
  */
object TravelTypeClassifier {
  /**
    * 根据Referer中的日期个数进行打标签
    * @param referer
    * @return
    */
  def travelType(referer: String): TravelTypeEnum = {
    //查看日期个数.
    val pattern: Pattern = Pattern.compile("(\\d{4})-(0\\d{1}|1[0-2])-(0\\d{1}|[12]\\d{1}|3[01])")
    //使用正则和url做匹配
    val matcher: Matcher = pattern.matcher(referer)
    //创建计数器
    var num = 0
    //看有没有匹配到
    while (matcher.find()){
      //如果匹配到,计数器+1
      num = num + 1
    }
    if (num == 1) {
      //匹配到了1次,单程
      TravelTypeEnum.OneWay
    } else if (num == 2) {
      //往返
      TravelTypeEnum.RoundTrip
    } else {
      //其它
      TravelTypeEnum.Unknown
    }
  }

}
