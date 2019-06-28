package cn.itheima.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

public class NewsProducer {
    /*
    原生kafka改造爬虫服务
     */
    private static KafkaProducer<String ,String > kafkaProducer;
    static {
        Properties props=new Properties();
        props.put("bootstrap.servers", "bigdata26-01:9092");
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer",
                "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer",
                "org.apache.kafka.common.serialization.StringSerializer");
        kafkaProducer = new KafkaProducer<String, String>(props);
    }

    public void sendNews(String news){
        ProducerRecord<String, String> record=new ProducerRecord<>("news",news);
        kafkaProducer.send(record);
        kafkaProducer.flush();
    }
}
