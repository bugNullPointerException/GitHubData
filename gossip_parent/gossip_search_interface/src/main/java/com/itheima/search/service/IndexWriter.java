package com.itheima.search.service;

import com.itheima.gossip.pojo.News;

import java.util.List;
//@service注解远程连接dubbox的

public interface IndexWriter {
    //存储到solr的方法
    public void saveBeans(List<News> news)throws Exception;
}
