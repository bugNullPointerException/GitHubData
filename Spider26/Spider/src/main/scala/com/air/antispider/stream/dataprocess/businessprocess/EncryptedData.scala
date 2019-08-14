package com.air.antispider.stream.dataprocess.businessprocess

import java.util.regex.{Matcher, Pattern}

import com.air.antispider.stream.common.util.decode.MD5
import org.apache.spark.rdd.RDD

/**
  * 对用户信息进行加密
  */
object EncryptedData {
  /**
    * 对身份证号进行加密
    * @param message
    * @return
    */
  def encryptedID(message: String): String = {
    //引入Md5工具类
    val md5 = new MD5
    //将Message赋值给一个临时变量
    var result = message
    //1. 先找到cookie
    val arr: Array[String] = message.split("#CS#")
    if (arr.length > 10) {
      var cookie = arr(10)
      //2. 找到身份证号?正则
      //身份证号正则
      val pattern: Pattern = Pattern.compile("(\\d{18})|(\\d{17}(\\d|X|x))|(\\d{15})")
      //使用正则对象校验cookie字符串,返回匹配结果对象
      val matcher: Matcher = pattern.matcher(cookie)
      //看是否能匹配上,能不能找到
      while (matcher.find()){
        //能找到,取出值
        val phone: String = matcher.group()
        //3. 替换/加密
        result = result.replace(phone, md5.getMD5ofStr(phone))
      }
      result
    }
    //4. 返回加密后的数据
    result
  }

  /**
    * 对用户的手机号进行加密操作
    *
    * @param messageRDD
    * @return
    */
  def encryptedPhone(messageRDD: RDD[String]): RDD[String] = {
    messageRDD.map(message => {
      //引入Md5工具类
      val md5 = new MD5
      //将Message赋值给一个临时变量
      var result = message
      //1. 先找到cookie
      val arr: Array[String] = message.split("#CS#")
      if (arr.length > 10) {
        var cookie = arr(10)
        //2. 找到手机号?正则
        //手机号正则
        val pattern: Pattern = Pattern.compile("((13[0-9])|(14[5|7])|(15([0-3]|[5-9]))|(17[0-9])|(18[0,5-9]))\\d{8}")
        //使用正则对象校验cookie字符串,返回匹配结果对象
        val matcher: Matcher = pattern.matcher(cookie)
        //看是否能匹配上,能不能找到
        while (matcher.find()){
          //能找到,取出值
          val phone: String = matcher.group()
          //3. 替换/加密
          result = result.replace(phone, md5.getMD5ofStr(phone))
        }


        result
      }
      //4. 返回加密后的数据
      result
    })

  }

}
