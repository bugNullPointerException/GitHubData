package com.itheima.cache.service.listener;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.itheima.cache.service.NewsCachService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class KeywordsConsumer implements MessageListener<String, String> {
    private Gson gson=new Gson();

    @Autowired
    private NewsCachService newsCachService;

    @Override
    public void onMessage(ConsumerRecord<String, String> data) {
        try {
            //执行的频率不能特别频繁
            String topKeyString = data.value();
           // List<Map> mapList = gson.fromJson(topKeyString,List.class);
            List<Map> mapList = JSON.parseArray(topKeyString, Map.class);
            for (Map map : mapList) {
                String keywords = (String) map.get("topKeywords");
                newsCachService.cacheNews(keywords);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
