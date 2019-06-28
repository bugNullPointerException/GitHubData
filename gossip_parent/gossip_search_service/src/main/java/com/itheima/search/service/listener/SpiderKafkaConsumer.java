package com.itheima.search.service.listener;

import com.google.gson.Gson;
import com.itheima.gossip.pojo.News;
import com.itheima.search.service.IndexWriter;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

@Component
public class SpiderKafkaConsumer implements MessageListener<String, String> {
    private  static Gson gson = new Gson();
    @Autowired
    private IndexWriter indexWriter;

    @Override
    public void onMessage(ConsumerRecord<String, String> record) {
        try {
            //消费topic:news中的新闻数据
            String newsJson = record.value();
            System.out.println(newsJson);
            //将json字符串格式的新闻数据转化为Java对象
            News news = gson.fromJson(newsJson, News.class);
            SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            //处理时间格式问题
            String time = news.getTime();
            Date date = format1.parse(time);
            time = format2.format(date);
            news.setTime(time);

            //调用索引写入服务
            indexWriter.saveBeans(Arrays.asList(news));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
