package com.gznx.controller;

import com.gznx.service.ExecutorService;
import com.gznx.websocket.LogMessageHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/test")
public class TestController {

    @Resource
    private ExecutorService executorService;

    @GetMapping("/list")
    public Map testHello() {
        Map map = new HashMap();
        map.put("name", "Jack");
        map.put("age", 18);
        return map;
    }
}
