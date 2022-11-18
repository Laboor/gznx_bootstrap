package com.gznx.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class PageController {

    @GetMapping("/")
    public String getIndex() {
        //初始返回首页
        return "index.html";
    }
}
