package com.itheima.pojo;

import java.util.Arrays;
import java.util.List;

public class QueryVo {
    private Item item;
    private Integer[] ids;

    private List<Item> itemList;

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public Integer[] getIds() {
        return ids;
    }

    public void setIds(Integer[] ids) {
        this.ids = ids;
    }

    public List<Item> getItemList() {
        return itemList;
    }

    public void setItemList(List<Item> itemList) {
        this.itemList = itemList;
    }

    @Override
    public String toString() {
        return "QueryVo{" +
                "item=" + item +
                ", ids=" + Arrays.toString(ids) +
                ", itemList=" + itemList +
                '}';
    }

    public QueryVo() {
    }
}

