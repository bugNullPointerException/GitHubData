package cn.itheima.version;

import cn.itheima.common.Constants;
import cn.itheima.dao.NewsDao;
import cn.itheima.pojo.News;
import cn.itheima.utils.HttpClientUtils;
import cn.itheima.utils.IdWorker;
import cn.itheima.utils.JedisUtil;
import com.google.gson.Gson;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NewsTencentMaster {
    private static Gson gson = new Gson();
    private static IdWorker idWorker=new IdWorker();
    private static JedisUtil jedisUtil=new JedisUtil();


    public static void main(String[] args) throws Exception {
        //确定url
        //热点新闻的
        String hotIndexUrl="https://pacaio.match.qq.com/irs/rcd?cid=137&token=d0f13d594edfc180f5bf6b845456f3ea&id=&ext=ent&num=200";
        List<News> hotNewsList =spiderNews(hotIndexUrl);
        addNews(hotNewsList);
        //非热点新闻的
        String noHotIndexUrl="https://pacaio.match.qq.com/irs/rcd?cid=58&token=c232b098ee7611faeffc46409e836360&ext=ent&page=0";
        pageSprider(noHotIndexUrl);


    }

    /**
     * 非热点新闻需要分页查询
     *
     */
    private static void pageSprider(String url) throws Exception {
        int page=1;
        while (true){
            //每页调用插叙每页的方法
            List<News> newsList = spiderNews(url);
            //判断如果为空了则结束
            if (newsList==null || newsList.size()<=0){
                //提示打印结束
                //System.out.println("数据写入成功");
                break;
            }
            addNews(newsList);
            url="https://pacaio.match.qq.com/irs/rcd?cid=58&token=c232b098ee7611faeffc46409e836360&ext=ent&page="+page;
            page++;
            System.out.println(page);
        }


    }

    //根据url获取json字符串
    private static List<News> spiderNews(String url) throws Exception {
        List<News> newsList=new ArrayList<News>();
        //获取json字符串
        String jsonString = HttpClientUtils.doGet(url);
        Map<String,Object> map = gson.fromJson(jsonString, Map.class);
        //获取其中的data部分
        //获取新闻列表数据
        List<Map<String,Object>> data = (List<Map<String, Object>>) map.get("data");
        for (Map<String, Object> newsMap : data) {
            String title = (String) newsMap.get("title");
            String docurl = (String) newsMap.get("url");
            Jedis jedis =jedisUtil.getJedis();
            Boolean sisme = jedis.sismember(Constants.DOCURL_SET, docurl);
            jedis.close();
            if (!sisme){
                String time = (String) newsMap.get("publish_time");
                String content = (String) newsMap.get("intro");
                String source = (String) newsMap.get("source");
                String editor = (String) newsMap.get("source");//没有editor

                //创建news，将解析到的新闻存放进来
                News news = new News();
                String id = idWorker.nextId() + "";
                news.setId(id);
                news.setTitle(title);
                news.setDocurl(docurl);
                news.setTime(time);
                news.setContent(content);
                news.setSource(source);
                news.setEditor(editor);
                newsList.add(news);
            }
        }
        return newsList;
    }

    /**
     * 通过新闻列表,将新闻列表数据保存到mysql
     * @param hotNewsList
     */
    private static void addNews(List<News> hotNewsList) {
        Jedis jedis = jedisUtil.getJedis();
        for (News news : hotNewsList) {
            //将数据保存到任务队列
            String newsJson = gson.toJson(news);
            jedis.lpush(Constants.NEWS_LIST,newsJson);
        }
        jedis.close();
    }
}
