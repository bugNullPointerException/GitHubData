package cn.itheima.news;

import cn.itheima.common.Constants;
import cn.itheima.dao.NewsDao;
import cn.itheima.pojo.News;
import cn.itheima.utils.HttpClientUtils;
import cn.itheima.utils.IdWorker;
import cn.itheima.utils.JedisUtil;
import com.google.gson.Gson;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class News163Spider {
    private static Gson gson = new Gson();
    private static IdWorker idWorker=new IdWorker();
    private static NewsDao newsDao =new NewsDao();
    private static JedisUtil jedisUtil=new JedisUtil();

    public static void main(String[] args) throws Exception {
        //确定爬取页面的url
        String indexUrl = "https://ent.163.com/special/000380VU/newsdata_index.js?callback=data_callback";

        //发送请求调用方法实现
        spiderNews(indexUrl);

    }

    //方法一,根据传入的url解析数据
    private static void spiderNews(String indexUrl) throws Exception {
        int page=2;
        while (true){
            //获得了页面的异步加载的json
            String jsonString = HttpClientUtils.doGet(indexUrl);
            //如果页面内容为空,则返回的jsonString 为空
            if (StringUtils.isEmpty(jsonString)){
                //没有数据了退出循环
                System.out.println("保存完毕");
                break;
            }
            //新闻页面异步加载,循环获取哥哥就业面的url
            //对该字符串进行处理,使其成为一个json格式
            int start = jsonString.indexOf("(");
            int end = jsonString.lastIndexOf(")");
            String json = jsonString.substring(start + 1, end);
            //解析json
            List<Map<String, Object>> maplist = gson.fromJson(json, List.class);
            //循环获取到所有的url

            for (Map<String, Object> map : maplist) {
                String docurl = (String) map.get("docurl");
                //去掉无用的url
                if (!docurl.contains("photoview") && docurl.contains("ent.163.com")) {
                    //判断如果docurl在redis中就不再进行
                    Jedis jedis = jedisUtil.getJedis();
                    Boolean sismember = jedis.sismember(Constants.DOCURL_SET, docurl);
                    jedis.close();
                    if (!sismember){
                        //!sismember为true表示不存在
                        //根据新闻的详情页发送请求
                        spiderItem(docurl);

                    }
                }
            }
            String pageStr="";
            if (page<10){
                pageStr="0"+page;
            }else{
                pageStr=page+"";
            }
            indexUrl = "https://ent.163.com/special/000380VU/newsdata_index_"+pageStr+".js?callback=data_callback";
            page++;
            System.out.println(page);
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
        news.setTitle(title);
        news.setTime(time);
        news.setSource(source);
        news.setContent(content);
        news.setEditor(editor);
        news.setDocurl(docurl);
        //调用dao层将数据保存到数据库中
        newsDao.saveNews(news);
        //保存成功后,说明该新闻爬取完毕,下次就不在爬取
        Jedis jedis = jedisUtil.getJedis();
        jedis.sadd(Constants.NEWS_LIST,docurl);
        jedis.close();
    }
}
