package com.itheima.gossip.pojo;

import java.io.Serializable;
import java.util.List;

/**
 * @ClassName PageBean
 * @Description TODO 封装的是前端的分页条件和后端的分页结果
 */
public class PageBean implements Serializable{
    //前端给后端的查询分页的条件
    private Integer page=1;//当前页码
    private Integer pageSize=10;//每页显示条数
    //后端给前端分页结果
    private Integer pageCount;//总记录数
    private Integer pageNum;//总页数
    private List<News> newsList;//当前页的数据结果

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getPageCount() {
        return pageCount;
    }

    public void setPageCount(Integer pageCount) {
        this.pageCount = pageCount;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public List<News> getNewsList() {
        return newsList;
    }

    public void setNewsList(List<News> newsList) {
        this.newsList = newsList;
    }
}
