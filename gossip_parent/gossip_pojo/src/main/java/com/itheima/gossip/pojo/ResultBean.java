package com.itheima.gossip.pojo;

import java.io.Serializable;

public class ResultBean  implements Serializable{
    private String keywords;
    private String startDate;
    private String endDate;
    private String editor;
    private String source;
    private PageBean pageBean;

    @Override
    public String toString() {
        return "ResultBean{" +
                "keywords='" + keywords + '\'' +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                ", editor='" + editor + '\'' +
                ", source='" + source + '\'' +
                ", pageBean=" + pageBean +
                '}';
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getEditor() {
        return editor;
    }

    public void setEditor(String editor) {
        this.editor = editor;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public PageBean getPageBean() {
        return pageBean;
    }

    public void setPageBean(PageBean pageBean) {
        this.pageBean = pageBean;
    }
}
