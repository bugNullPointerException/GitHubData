package com.air.antispider.stream.dataprocess.businessprocess

import scala.collection.mutable.ArrayBuffer

/**
  * 统计高频IP
  */
object IpOperation {
  /**
    * 判断客户端IP是否属于高频IP
    * @param remoteAddr
    * @param blackList
    * @return
    */
  def isBlackIP(remoteAddr: String, blackList: ArrayBuffer[String]): Boolean = {
    //遍历blackList,看remoteAddr和里面的ip是否相等
    for (blackIP <- blackList) {
      if (remoteAddr.equals(blackIP)) {
          //如果相等,是高频IP
          return true
      }
    }
    false
  }

}
