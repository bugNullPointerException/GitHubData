package com.itheima.gossip.service;

import com.itheima.gossip.pojo.News;
import com.itheima.gossip.pojo.ResultBean;

import java.util.List;

public interface IndexSearcherService {
    //查询服务的接口
    public List<News> findByKeywords (String keywords) throws Exception;
    //条件查询分页
    public ResultBean findByQuery(ResultBean resultBean)throws Exception;
}
