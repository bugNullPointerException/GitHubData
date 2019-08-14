package com.csair.b2c.gciantispider.offlinecalculation.data

import org.apache.spark.{SparkConf, SparkContext}
/**
  * Created by wangsenfeng on 2018/3/21.
  */
object DataTransformation {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local").setAppName("visualization").setMaster("local[*]")
    val sc = new SparkContext(conf)
    //计数器
    var i = 0
    println("/ita/intl/zh/shop/ss".matches("^.*/ita/intl/zh/shop/.*$"))
    println("/modules/permissionnew/csair".matches("^.*/modules/permissionnew/.*$"))
    println("upp_payment/pay/csair".matches("^.*upp_payment/pay/.*$"))
    val request = sc.textFile("C:\\Users\\焦海峰\\Desktop\\反扒参考资料\\数据样本\\part-00000")
      .map(x => {
        var y = x
        //每隔50次
        if (i % 5 == 0) {
          y = y.replace("/B2C40/query/jaxb/direct/query.ao", "/ita/intl/zh/shop/csair")
        }
        if (i % 201 == 0) {
          y = y.replace("/B2C40/query/jaxb/direct/query.ao", "/modules/permissionnew/csair").replace("192.168.56.1", "243.234.12.43")
        }
        if (i % 701 == 0) {
          y = y.replace("/B2C40/query/jaxb/direct/query.ao", "/modules/permissionnew/csair")
        }
        if (i % 1001 == 0) {
          y = y.replace("/B2C40/query/jaxb/direct/query.ao", "upp_payment/pay/csair")
        }
        if (i % 2001 == 0) {
          y = y.replace("192.168.56.1", "243.234.12.43")
        }
        if (i % 200 == 0|| i % 402 == 0 || i % 2002 == 0 || i % 502 == 0) {
          y = y.replace("National", "Internatinal")
        }
        i=i+1
        y
      }
      ).repartition(1).saveAsTextFile("C:\\Users\\焦海峰\\Desktop\\反扒参考资料\\数据样本\\test")
    //sc.textFile("E:\\here\\工作文档\\23、项目研发\\反爬虫项目\\教案\\参考资料\\数据样本\\test").filter(_.matches("^.*/ita/intl/zh/shop/.*$")).foreach(println)
   // sc.textFile("E:\\here\\工作文档\\23、项目研发\\反爬虫项目\\教案\\参考资料\\数据样本\\test").filter(_.matches("^.*/modules/permissionnew/.*$")).foreach(println)
    //sc.textFile("E:\\here\\工作文档\\23、项目研发\\反爬虫项目\\教案\\参考资料\\数据样本\\test").filter(_.matches("^.*upp_payment/pay/.*$")).foreach(println)
  }
}
