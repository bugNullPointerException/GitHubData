package com.air.antispider.stream.common.util.HDFS

import com.air.antispider.stream.common.util.jedis.{JedisConnectionUtil, PropertiesUtil}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.apache.spark.sql.{DataFrame, Row, SQLContext}
import redis.clients.jedis.JedisCluster

/**
 * 黑名单保存到HDFS服务
 */
object BlackListToHDFS {
  /**
    * 保存黑名单到HDFS
    *
    * @param antiBlackListRDD：传入黑名单RDD
    * @param sqlContext：传入入sqlContext创建DataFrame
    */
  def saveAntiBlackList(antiBlackListRDD: RDD[Row], sqlContext: SQLContext) ={

    //构建DataFrame
    val tableCols = List("keyExpTime","key","value")
    val schemaString=tableCols.mkString(" ")
    val schema = StructType(schemaString.split(" ").map(fieldName => StructField(fieldName, StringType, true)))
    val dataFrame: DataFrame = sqlContext.createDataFrame(antiBlackListRDD,schema)
    val path: String = PropertiesUtil.getStringByKey("blackListPath","HDFSPathConfig.properties")
    HdfsSaveUtil.save(dataFrame,null,path)
  }

  def saveAntiOcpBlackList(): Unit ={


  }




}
