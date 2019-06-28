package com.itheima.gossip.mapper;

import com.itheima.gossip.pojo.News;
import org.springframework.stereotype.Repository;

import java.util.List;


public interface NewsMapper {
    //通过ID查询部分数据
    List<News> queryListByMaxId(String id);
    //通过ID查询部分数据的最大ID
    String queryNextMaxIdByMaxId(String id);
}
