package com.itheima.gossip.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.itheima.gossip.pojo.News;
import com.itheima.gossip.pojo.ResultBean;
import com.itheima.gossip.service.IndexSearcherService;
import com.itheima.search.service.IndexSearcher;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
//服务的调用者,提供本地服务,不适用dubbo注解
@Service

public class IndexSearcherServiceImpl implements IndexSearcherService {
    @Reference
    private IndexSearcher indexSearcher;


    @Override
    public List<News> findByKeywords(String keywords) throws Exception {
        //调用服务查询
        List<News> newsList = indexSearcher.findByKeywords(keywords);
        //如果返回的结果有问题,需要在返回前在这里处理
        //当前前段页面显示content过长,截取即可
        for (News news : newsList) {
            String content =news.getContent();
            //如果高亮就不截取,因为solr高亮默认处理100个字符
            //判断高亮截取是因为,直接截取可能会截取掉标签部分,导致两次循环合并
            if (!content.contains("<font color='red'>")&&content.length()>110){
                //如果没有高亮,且长度过长就截取
                content=content.substring(0,107)+"...";
                news.setContent(content);
            }
        }

        //直接返回给controller
        return newsList;
    }

    //条件查询及分页查询

    @Override
    public ResultBean findByQuery(ResultBean resultBean) throws Exception {
        //调用索引服务,将查询条件传入,将返回结果封装到resultbean中
        resultBean = indexSearcher.findByQuery(resultBean);
        //如果返回的结果有问题，需要在返回前在这里处理
        //当前前端页面显示content过长，截取即可
        for (News news : resultBean.getPageBean().getNewsList()) {
            String content = news.getContent();
//            if(content.contains("<font color='red'>")){//当前情况是有高亮
//                //如果有高亮不截取，solr做高亮时，只处理前100个字符
//            }
            if(!content.contains("<font color='red'>")&&content.length()>110){//如果没有高亮肯定截取
                content = content.substring(0, 107) + "...";
                news.setContent(content);
            }
        }
        return resultBean;
    }

}
