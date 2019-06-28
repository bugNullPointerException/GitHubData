package com.itheima.cache.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.itheima.cache.service.NewsCachService;
import com.itheima.gossip.pojo.PageBean;
import com.itheima.gossip.pojo.ResultBean;
import com.itheima.search.service.IndexSearcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Service
public class NewsCachServiceImpl implements NewsCachService {
    /*
   热词搜索服务,需要两种功能
   1. 提供关键词,查询索引库
   2.将热词封装到redis中
    */
    @Reference
    private IndexSearcher indexSearcher;
    @Autowired
    private JedisPool jedisPool;

    @Override
    public void cacheNews(String keywords) throws Exception {
        ResultBean resultBean = new ResultBean();
        //因为后面调用需要用到pagebean,所以不能为空
        PageBean pageBean = new PageBean();
        resultBean.setPageBean(pageBean);
        //查询到的是一页的数据
        resultBean.setKeywords(keywords);
        resultBean = indexSearcher.findByQuery(resultBean);
        //获取总页数
        Integer pageNum = resultBean.getPageBean().getPageNum();
        if (pageNum > 0) {
            if (pageNum > 5) {
                pageNum = 5;
            }
            //目的是为了循环获得五页数据
            for (int i = 1; i <= pageNum; i++) {
                //如何获得每一页数据
                resultBean.getPageBean().setPage(i);
                resultBean =indexSearcher.findByQuery(resultBean);
                String resultBeanString = JSON.toJSONString(resultBean);
                Jedis jedis = jedisPool.getResource();
                //缓存resultBean
                //查询到后直接缓存.
                jedis.set(keywords+":"+i,resultBeanString);
                jedis.close();
            }
        }
    }
}
