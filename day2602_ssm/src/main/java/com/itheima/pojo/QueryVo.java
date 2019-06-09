package com.itheima.pojo;

public class QueryVo {
    private Item item;

    @Override
    public String toString() {
        return "QueryVo{" +
                "item=" + item +
                '}';
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public QueryVo() {
    }

    public QueryVo(Item item) {
        this.item = item;
    }
}

