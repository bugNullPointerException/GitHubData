package com.csair.b2c.gciantispider.offlinecalculation.util

import java.util.Properties

/**
  * Created by wangsf on 8/11/2017.
  */
object SparkMySqlProperties {
  def getProperty(): Properties = {
    //创建Properties存储数据库相关属性
    val prop = new Properties()
    prop.put("user", "root")
    prop.put("password", "root")
    prop
  }
}
