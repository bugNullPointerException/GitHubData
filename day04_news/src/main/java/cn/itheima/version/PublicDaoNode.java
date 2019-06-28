package cn.itheima.version;

import cn.itheima.common.Constants;
import cn.itheima.dao.NewsDao;
import cn.itheima.pojo.News;

import cn.itheima.producer.NewsProducer;
import cn.itheima.utils.JedisUtil;
import com.google.gson.Gson;
import redis.clients.jedis.Jedis;

import java.util.List;

public class PublicDaoNode {
    private static Gson gson = new Gson();
    private static NewsDao newsDao =new NewsDao();
    private static JedisUtil jedisUtil=new JedisUtil();
    private static NewsProducer newsProducer=new NewsProducer();
    //书写保存数据的入口
    public static void main(String[] args) {
        while (true){
            Jedis jedis = jedisUtil.getJedis();
            List<String> list = jedis.brpop(200, Constants.NEWS_LIST);

            if (list==null || list.size()<=0){
                //表示任务队列没有数据了
                jedis.close();
                break;
            }
            //证明任务队列中有数据
            News news = gson.fromJson(list.get(1), News.class);
            //判断该数据是否已经保存
            Boolean sismem = jedis.sismember(Constants.DOCURL_SET, news.getDocurl());
            if (!sismem){
                //将该数据保存到下载列表中
                jedis.sadd(Constants.DOCURL_SET,news.getDocurl());
                //保存数据到kafka
                newsProducer.sendNews(list.get(1));

                //证明不存在,保存数据
                newsDao.saveNews(news);
            }
            jedis.close();
        }
    }
}
