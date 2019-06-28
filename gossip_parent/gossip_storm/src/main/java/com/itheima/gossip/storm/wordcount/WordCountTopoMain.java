package com.itheima.gossip.storm.wordcount;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.kafka.bolt.KafkaBolt;
import org.apache.storm.kafka.bolt.mapper.FieldNameBasedTupleToKafkaMapper;
import org.apache.storm.kafka.bolt.selector.DefaultTopicSelector;
import org.apache.storm.kafka.spout.KafkaSpout;
import org.apache.storm.kafka.spout.KafkaSpoutConfig;
import org.apache.storm.topology.IBasicBolt;
import org.apache.storm.topology.IRichSpout;
import org.apache.storm.topology.TopologyBuilder;

import java.util.Properties;

public class WordCountTopoMain {
    public static void main(String[] args) {
        //创建topology
        TopologyBuilder topologyBuilder=new TopologyBuilder();
        KafkaSpoutConfig.Builder<String, String> build= KafkaSpoutConfig.builder("bigdata26-01:9092","logs");
        build.setGroupId("hello_storm");
        //默认从头开始消费这里设置从最新的一条数据开始消费
        build.setFirstPollOffsetStrategy(KafkaSpoutConfig.FirstPollOffsetStrategy.LATEST);
        KafkaSpoutConfig<String, String> kafkaSpoutConfig = build.build();
        KafkaSpout<String,String> kafkaSpout =new KafkaSpout<>(kafkaSpoutConfig);
        //封装kafkaspout
        topologyBuilder.setSpout("kafkaSpout",kafkaSpout);
        //封装splitbolt
        topologyBuilder.setBolt("splitbolt",new SplitBolt()).shuffleGrouping("kafkaSpout");
        //封装wordcountbolt
        topologyBuilder.setBolt("wordcountbolt",new WordCountBolt()).shuffleGrouping("splitbolt");
        //封装kafkabot
        Properties props = new Properties();
        props.put("bootstrap.servers", "bigdata26-01:9092");
        props.put("acks", "all");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        KafkaBolt kafkaBolt=new KafkaBolt().withProducerProperties(props)
                .withTopicSelector(new DefaultTopicSelector("keywords"))
                .withTupleToKafkaMapper(new FieldNameBasedTupleToKafkaMapper());
        topologyBuilder.setBolt("kadkaBolt",kafkaBolt).shuffleGrouping("wordcountbolt");
        //运行topology
        LocalCluster localCluster=new LocalCluster();
        Config config=new Config();
        localCluster.submitTopology("wordcount",config,topologyBuilder.createTopology());
    }
}
