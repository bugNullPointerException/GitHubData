package com.itheima.search.service;

import com.itheima.gossip.pojo.News;
import com.itheima.gossip.pojo.ResultBean;

import java.util.List;

public interface IndexSearcher {
    //实现基本查询
    public List<News> findByKeywords(String keywords) throws Exception;
    //条件查询
    public ResultBean findByQuery(ResultBean resultBean)throws  Exception;
}
