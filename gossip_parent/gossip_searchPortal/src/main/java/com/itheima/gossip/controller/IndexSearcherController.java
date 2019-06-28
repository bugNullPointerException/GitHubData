package com.itheima.gossip.controller;

import com.itheima.gossip.pojo.News;
import com.itheima.gossip.pojo.PageBean;
import com.itheima.gossip.pojo.ResultBean;
import com.itheima.gossip.service.IndexSearcherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLDecoder;
import java.util.List;

@RestController
public class IndexSearcherController {
    //获取页面请求,调用service服务返回给页面
    @Autowired
    private IndexSearcherService indexSearcherService;

    @RequestMapping("/s")
    @ResponseBody
    public List<News> findByKeywords(String keywords) {
            //接受数据,判断
            if (StringUtils.isEmpty(keywords)) {
                //为空就没有数据,跳转首页
                return null;
            }
        try {
            //不为空就调用service层

            List<News> newsList = indexSearcherService.findByKeywords(keywords);
            return newsList;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }



    @RequestMapping("/ps")
    public ResultBean findByQuery(ResultBean resultBean){
        //判断封装的查询条件是否正确
        if(resultBean==null){
            return null;
        }
        if(StringUtils.isEmpty(resultBean.getKeywords())){
            return null;
        }
        //前端条件中没有分页的参数，也就不会有pageBean对象
        if(resultBean.getPageBean()==null){//如果resultBean中的pageBean是null需要先创建
            PageBean pageBean = new PageBean();
            resultBean.setPageBean(pageBean);
        }
        try {
            String keywords = URLDecoder.decode(resultBean.getKeywords(), "utf-8");
            System.out.println("keywords：" + keywords);
            resultBean.setKeywords(keywords);
            return indexSearcherService.findByQuery(resultBean);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
