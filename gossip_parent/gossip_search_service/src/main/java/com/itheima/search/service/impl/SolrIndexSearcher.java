package com.itheima.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.itheima.gossip.pojo.News;
import com.itheima.gossip.pojo.ResultBean;
import com.itheima.search.service.IndexSearcher;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class SolrIndexSearcher implements IndexSearcher {
    @Autowired
    private CloudSolrServer cloudSolrServer;
    @Override
    public List<News> findByKeywords(String keywords) throws Exception {
        //实现基本查询
        SolrQuery solrQuery=new SolrQuery("text:"+keywords);
        //设置高亮显示
        solrQuery.setHighlight(true);//开启高亮显示
        solrQuery.addHighlightField("title");
        solrQuery.addHighlightField("content");
        solrQuery.setHighlightSimplePre("<font color='red'>");
        solrQuery.setHighlightSimplePost("</font>");
        //solrQuery.setHighlightFragsize();//默认值是100,设置处理高亮附近共100个字符.

        //查询,返回结果
        QueryResponse response = cloudSolrServer.query(solrQuery);
        //如果封装的结果中有特殊类型,如日期,就不能直接封装到String中,要手动封装
        //获取高亮部分
        Map<String ,Map<String ,List<String >>> highlighting =response.getHighlighting();
        //获取未高亮部分
        SolrDocumentList documents = response.getResults();
        System.out.println("总记录时:"+documents.getNumFound());
        SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //创建一个集合封住那个数据
        List<News> newsList=new ArrayList<>();
        for (SolrDocument document : documents) {
            String id = (String) document.get("id");
            String title = (String) document.get("title");
            String content = (String) document.get("content");
            String source = (String) document.get("source");
            String editor = (String) document.get("editor");
            String docurl = (String) document.get("docurl");
            //solr时间是以0时区的时间,底层会根据系统时区自动转换时间,与原始数据存在时间查
            Date time = (Date) document.get("time");
            time.setTime(time.getTime()-(1000*60*60*8));
            String newTime=format.format(time);

            Map<String ,List<String >> filedsMap =highlighting.get(id);
            List<String > titleList =filedsMap.get("title");
            if (titleList!=null && titleList.size()>0){
                //必须有高亮数据才去获取
                title=titleList.get(0);
            }
            List<String > contentList=filedsMap.get("content");
            if (contentList!=null && contentList.size()>0){
                //必须有高亮数据才去获取
                content=contentList.get(0);
            }

            //手动封装数据到Javabean中
            News news=new News();
            news.setId(id);
            news.setTitle(title);
            news.setContent(content);
            news.setSource(source);
            news.setEditor(editor);
            news.setDocurl(docurl);
            news.setTime(newTime);

            newsList.add(news);
        }

        return newsList;
    }



    /**
     * 条见查询和分页
     * @param resultBean 参数是controller的封装的查询条件
     * @return
     * @throws Exception
     */
    @Override
    public ResultBean findByQuery(ResultBean resultBean) throws Exception {
        //1.1实现基本查询
        SolrQuery solrQuery=new SolrQuery("text:"+resultBean.getKeywords());
        //1.2设置高亮显示
        solrQuery.setHighlight(true);//开启高亮显示
        solrQuery.addHighlightField("title");
        solrQuery.addHighlightField("content");
        solrQuery.setHighlightSimplePre("<font color='red'>");
        solrQuery.setHighlightSimplePost("</font>");
        //solrQuery.setHighlightFragsize();//默认值是100，处理高亮附近100个字符
        //1.3过滤查询 是在基本查询的基础上进行过滤
        //过滤查询条件一定存在吗？
        //日期格式06/17/2019 16:26:40  solr格式
        SimpleDateFormat format1 = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

        String startDate = resultBean.getStartDate();
        String endDate = resultBean.getEndDate();
        if(!StringUtils.isEmpty(startDate)&&!StringUtils.isEmpty(endDate)){
            //有过滤条件才转化
            Date dateStart = format1.parse(startDate);
            startDate = format2.format(dateStart);
            Date dateEnd = format1.parse(endDate);
            endDate = format2.format(dateEnd);

            //查询条件当前数据的时间要在起始和结束时间范围内
            solrQuery.addFilterQuery("time:["+startDate+" TO "+endDate+"]");
        }
        if(!StringUtils.isEmpty(resultBean.getEditor())){
            solrQuery.addFilterQuery("editor:"+resultBean.getEditor());
        }
        if(!StringUtils.isEmpty(resultBean.getSource())){
            solrQuery.addFilterQuery("source:"+resultBean.getSource());
        }
        //添加分页查询条件
        Integer page = resultBean.getPageBean().getPage();//当前页码
        Integer pageSize = resultBean.getPageBean().getPageSize();
        //每页显示的条数
        if (page==null){
            page=1;
        }
        if (pageSize==null){
            pageSize=10;
        }
        solrQuery.setStart((page-1)*pageSize);//起始
        solrQuery.setRows(pageSize);//显示多少行
        //1.5按照时间排序
        /*两种方式效果一样
        SolrQuery.SortClause sortClause=new SolrQuery.SortClause("time",SolrQuery.ORDER.desc);
        solrQuery.setSort(sortClause);*/
        solrQuery.setSort("time",SolrQuery.ORDER.desc);

        //查询，返回结果
        QueryResponse response = cloudSolrServer.query(solrQuery);
        //如果封装的结果中有特殊类型，比如日期date，是不能直接封装为String,要手动封装
        //获取高亮的部分
        Map<String, Map<String, List<String>>> highlighting = response.getHighlighting();
        //获取的是未被高亮的
        SolrDocumentList documents = response.getResults();
        long numFound = documents.getNumFound();
        //手动封装
        List<News> newsList = new ArrayList<>();
        for (SolrDocument document : documents) {
            String id = (String) document.get("id");
            String title = (String) document.get("title");
            String content = (String) document.get("content");
            Date time = (Date) document.get("time");
            //时间多了8个小时，需要减去
            time = new Date(time.getTime() - (1000 * 60 * 60 * 8));
            System.out.println(time);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String time_new = format.format(time);
            String source = (String) document.get("source");
            String editor = (String) document.get("editor");
            String docurl = (String) document.get("docurl");

            Map<String, List<String>> filedsMap = highlighting.get(id);
            List<String> titleList = filedsMap.get("title");
            if(titleList!=null&&titleList.size()>0){
                //必须有高亮数据才去获取
                title = titleList.get(0);
            }
            List<String> contentList = filedsMap.get("content");
            if(contentList!=null&&contentList.size()>0){
                //必须有高亮数据才去获取
                content = contentList.get(0);
            }

            //创建news对象
            News news = new News();
            news.setId(id);
            news.setContent(content);
            news.setTitle(title);
            news.setTime(time_new);
            news.setSource(source);
            news.setEditor(editor);
            news.setDocurl(docurl);
            newsList.add(news);
        }

        //封装结果  目前只有5个查询条件，没有任何返回的内容
        //前端封装的条件中是不包含pageBean相关的内容，所以封装的resultBean的pageBean是null
        //如果是基本数据类型是可以强转，如果是封装类型，是不能强转

        //当pageBean不为null时即可封装数据
        resultBean.getPageBean().setPageCount((int) numFound);//将总记录数封装到pageBean
        //将总记录数封装到pagebean中
        //计算总页数
        double pageNum = Math.ceil((double) numFound / pageSize);
        resultBean.getPageBean().setPageNum((int) pageNum);

        resultBean.getPageBean().setNewsList(newsList);//将当前页结果数据分装到pageBean

        return resultBean;
    }
}
