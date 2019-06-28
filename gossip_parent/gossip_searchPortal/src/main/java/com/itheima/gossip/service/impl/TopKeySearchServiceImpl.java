package com.itheima.gossip.service.impl;

import com.itheima.gossip.service.TopKeySearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Tuple;

import java.util.*;

@Service
public class TopKeySearchServiceImpl implements TopKeySearchService {
    @Autowired
    private JedisPool jedisPool;
    @Override
    public List<Map<String, Object>> findTopKeyByNum(Integer num) {
        //要返回的json对象
        List<Map<String,Object>> topkeyList=new ArrayList<>();
        //从redis中获取排行榜中的前10条数据
        Jedis jedis=jedisPool.getResource();
        Set<Tuple> tuples=jedis.zrevrangeWithScores("bigdata:gossip:topkey",0,num);
        //将排行榜中的数据封装到list中
        for (Tuple tuple: tuples){
            Map<String ,Object> topkeyMap=new HashMap<>();
            String topkey =tuple.getElement();

            topkeyMap.put("topkey",topkey);

            double score = tuple.getScore();
            topkeyMap.put("score",score);
            topkeyList.add(topkeyMap);
        }
        jedis.close();
        return topkeyList;
    }
}
