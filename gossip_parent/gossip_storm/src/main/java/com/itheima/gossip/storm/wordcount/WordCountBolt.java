package com.itheima.gossip.storm.wordcount;

import com.alibaba.fastjson.JSON;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WordCountBolt extends BaseBasicBolt {
    private Map<String, Integer> wordCountMap = new HashMap<>();

    @Override
    public void execute(Tuple tuple, BasicOutputCollector basicOutputCollector) {
        String keywords = tuple.getStringByField("keywords");
        Integer count = wordCountMap.get(keywords);
        if (count == null || count <= 0) {
            //没有存过
            wordCountMap.put(keywords, 1);
        } else {
            wordCountMap.put(keywords, count + 1);
        }
        //不能统计一次就封装一次,封装到一个list集合中
        List<Map> list = new ArrayList<>();
        for (String key : wordCountMap.keySet()) {
            Integer score = wordCountMap.get(key);
            Map<String, Object> map = new HashMap<>();
            map.put("topKeywords", key);
            map.put("score", score);
            //不能每次都发
            if (score >= 3) {
                //次数大于3,此时才认为是热词
                list.add(map);
            }
        }

        String jsonString = JSON.toJSONString(list);
       // System.out.println(jsonString);
        basicOutputCollector.emit(new Values(jsonString));
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        //注意必须叫message 是下游kafkabolt要求的
        outputFieldsDeclarer.declare(new Fields("message"));
    }
}
