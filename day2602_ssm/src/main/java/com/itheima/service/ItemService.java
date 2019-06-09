package com.itheima.service;

import com.itheima.pojo.Item;
import com.itheima.pojo.QueryVo;

import java.util.List;

public interface ItemService {

    List<Item> findAll();

    Item editItemById(int i);

    int updateItem(Item item);

    List<Item> queryItem(QueryVo vo);
}
