package com.itheima.gossip.controller;

import com.itheima.gossip.service.TopKeySearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class TopKeySearchController {
    @Autowired
    private TopKeySearchService topKeySearchService;

    @RequestMapping("/top")
    public List<Map<String, Object>> findTopByNum(Integer num){
        return topKeySearchService.findTopKeyByNum(num);
    }
}
