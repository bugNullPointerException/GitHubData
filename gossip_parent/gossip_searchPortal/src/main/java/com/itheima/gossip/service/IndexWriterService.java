package com.itheima.gossip.service;

public interface IndexWriterService {
    //调用dao查询数据调用solrservice保存数据

    public void indexWriter() throws Exception;

}
