package com.itheima.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.itheima.gossip.pojo.News;
import com.itheima.search.service.IndexWriter;
import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
//提供服务方,需要使用dubbo的注解
@Service
public class IndexWriterImpl implements IndexWriter {
    @Autowired
    private CloudSolrServer cloudSolrServer;

    @Override
    public void saveBeans(List<News> news) throws Exception {
        cloudSolrServer.addBeans(news);
        cloudSolrServer.commit();
    }
}
