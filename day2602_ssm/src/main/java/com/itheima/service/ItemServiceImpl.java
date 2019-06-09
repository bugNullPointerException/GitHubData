package com.itheima.service;

import com.itheima.mapper.ItemMapper;
import com.itheima.pojo.Item;
import com.itheima.pojo.QueryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class ItemServiceImpl implements ItemService {
    @Autowired
    private ItemMapper mapper;
    public List<Item> findAll() {
        List<Item> list = mapper.findAll();
        return list;
    }
//  查询修改的商品信息
    public Item editItemById(int id) {
        Item item=mapper.editItemById(id);
        return item;
    }

    //修改商品信息
    public int updateItem(Item item) {
        int i =mapper.updateItem(item);
        return i;
    }

    //条件查询
    public List<Item> queryItem(QueryVo vo) {
        List<Item> list=mapper.queryItem(vo);
        return list;
    }
}
