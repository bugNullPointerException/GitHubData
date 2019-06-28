package com.itheima.gossip.service;

import java.util.List;
import java.util.Map;

public interface TopKeySearchService {
    //查询前10
    public List<Map<String ,Object>> findTopKeyByNum(Integer num);
}
