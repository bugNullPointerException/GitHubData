package cn.itheima.common;

public class Constants {
    //public  final static String NEWS_163_DOCURL="bigdata:gossip:news163:docurl";
    //public final static String NEWS_TENCENT_DOCURL="bigdata:gossip:newstencent:docurl";
    //将163和腾讯新闻内容放到一起进行去重判断
    public final static String DOCURL_SET="bigdata:gossip:set:docurl";
    //增加两个任务队列
    //将需要爬取的url放到list队列
    public final static String DOCURL_LIST="bigdata:gossip:list:docurl";
    //将需要保存的news放到此list队列
    public final static String NEWS_LIST="bigdata:gossip:list:news";

}
