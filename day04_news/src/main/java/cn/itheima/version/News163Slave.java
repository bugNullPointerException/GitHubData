package cn.itheima.version;

import cn.itheima.common.Constants;
import cn.itheima.dao.NewsDao;
import cn.itheima.pojo.News;
import cn.itheima.utils.HttpClientUtils;
import cn.itheima.utils.IdWorker;
import cn.itheima.utils.JedisUtil;
import com.google.gson.Gson;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import redis.clients.jedis.Jedis;

import java.util.List;

public class News163Slave {

    private static Gson gson = new Gson();
    private static IdWorker idWorker=new IdWorker();
    private static JedisUtil jedisUtil=new JedisUtil();
    //创建分布式入口main
    public static void main(String[] args) throws Exception {
        //循环获取redis的值
        while (true){
            Jedis jedis = jedisUtil.getJedis();
            List<String> list = jedis.brpop(200, Constants.DOCURL_LIST);
            jedis.close();
            //brpop该方法是阻塞式取法,需要提供超时时间,单位是毫秒值,返回值有两位,第一位是key,第二位是value
            if (list!=null && list.size()>1){
                //证明里面有内容
                spiderItem(list.get(1));
            }else {
                break;
            }
        }

    }

    //根据详情页的url来获取页面信息
    private static void spiderItem(String docurl) throws Exception {
        String html = HttpClientUtils.doGet(docurl);
        //解析详情页
        Document document = Jsoup.parse(html);
        //创建对应的数据库表及pojo类
        //获取对应的数据

        String title = document.select("#epContentLeft > h1").get(0).text();
        String timeAndSource = document.select("#epContentLeft > div.post_time_source").get(0).text();
        String[] splits = timeAndSource.split("　来源: ");
        String time =splits[0];
        String source =splits[1].split(" ")[0];
        String content = document.select("#endText > p[class!=f_center]").text();
        String ep_editor = document.select(".ep-editor").get(0).text();
        String editor = ep_editor.split("：")[1];

        //将解析到的字段封装到pojo中
        News news=new News();
        String id = idWorker.nextId()+"";
        news.setId(id);
        news.setDocurl(docurl);
        news.setTitle(title);
        news.setTime(time);
        news.setSource(source);
        news.setContent(content);
        news.setEditor(editor);

        System.out.println(docurl);

        //将new对象转成json字符串放到redis队列中
        String newsJson = gson.toJson(news);
        Jedis jedis = jedisUtil.getJedis();
        jedis.lpush(Constants.NEWS_LIST,newsJson);
        jedis.close();
    }
}
