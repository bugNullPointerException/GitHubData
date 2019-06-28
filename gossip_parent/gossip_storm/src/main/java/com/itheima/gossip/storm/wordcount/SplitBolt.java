package com.itheima.gossip.storm.wordcount;

import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

public class SplitBolt extends BaseBasicBolt {
    @Override
    public void execute(Tuple tuple, BasicOutputCollector basicOutputCollector) {
        String log = tuple.getString(4);
        //System.out.println(log);
        if (log.contains("#CS#")) {
            int indexOf = log.lastIndexOf("#CS#");
            String keywords = log.substring(indexOf + 4);
            //发送数据
            basicOutputCollector.emit(new Values(keywords));
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("keywords"));
    }
}
