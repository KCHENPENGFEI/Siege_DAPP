package com.example.zjusiege.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class Page {

    @RequestMapping("/hello")
    public String hello(){
        System.out.println("Hello!!!");
        return "hello";
    }
}