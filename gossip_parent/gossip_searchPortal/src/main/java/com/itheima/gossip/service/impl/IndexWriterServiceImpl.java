package com.itheima.gossip.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.itheima.gossip.mapper.NewsMapper;
import com.itheima.gossip.pojo.News;
import com.itheima.gossip.service.IndexWriterService;
import com.itheima.search.service.IndexWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
//这里使用的是spring的注解,因为它是调用者,不需要上传方法,都是在本地调用,所以不用上传

@Service
public class IndexWriterServiceImpl implements IndexWriterService{
    @Autowired
    private JedisPool jedisPool;
    @Autowired
    private NewsMapper newsMapper;
    @Reference
    private IndexWriter indexWriter;
    @Override
    public void indexWriter() throws Exception {
        //通过jedis获取最大id
        Jedis jedis = jedisPool.getResource();
        String maxId=jedis.get("bigdata:gossip:id");
        if (StringUtils.isEmpty(maxId)){
        //设置出事最大ID
            maxId=0+"";

        }
        jedis.close();
        //调用dao查数据
        while (true){
            List<News> newsList = newsMapper.queryListByMaxId(maxId);
            if (newsList == null || newsList.size() <= 0) {
                //没有数据结束循环,此时保存最大id
                jedis = jedisPool.getResource();
                jedis.set("bigdata:gossip:id",maxId);
                jedis.close();
                break;
            }
            //solrcloud中time格式是时间按格式
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat formatNew = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            for (News news : newsList) {
                Date date = simpleDateFormat.parse(news.getTime());
                String time = formatNew.format(date);
                news.setTime(time);
            }
            //调用searchService保存数据到solrcloud中
            indexWriter.saveBeans(newsList);
            //获取最大索引
            maxId=newsMapper.queryNextMaxIdByMaxId(maxId);
        }

    }
}
