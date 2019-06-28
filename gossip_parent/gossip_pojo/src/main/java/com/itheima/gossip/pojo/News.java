package com.itheima.gossip.pojo;

import org.apache.solr.client.solrj.beans.Field;

import java.io.Serializable;

//javabean 中需要存储到磁盘或者需要网络传输的时候需要添加序列化
public class News implements Serializable {
    @Field
    private String id;
    @Field
    private String title;
    @Field
    private String content;
    @Field
    private String time;
    @Field
    private String source;
    @Field
    private String docurl;
    @Field
    private String editor;

    @Override
    public String toString() {
        return "News{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", time='" + time + '\'' +
                ", source='" + source + '\'' +
                ", docurl='" + docurl + '\'' +
                ", editor='" + editor + '\'' +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDocurl() {
        return docurl;
    }

    public void setDocurl(String docurl) {
        this.docurl = docurl;
    }

    public String getEditor() {
        return editor;
    }

    public void setEditor(String editor) {
        this.editor = editor;
    }
}
