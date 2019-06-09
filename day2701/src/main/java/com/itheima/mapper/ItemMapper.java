package com.itheima.mapper;

import com.itheima.pojo.Item;
import com.itheima.pojo.QueryVo;

import java.util.List;

public interface ItemMapper {
//    实现查询所有的功能
    List<Item> findAll();

    Item editItemById(int id);

    int updateItem(Item item);

    List<Item> queryItem(QueryVo vo);
}
