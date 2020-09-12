package com.example.springapidemo.controller;

import com.example.utils.utlisdemo.test.TestDemo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *目的：检测api-demo依赖utilsDemo，可以访问utils里的getCurDate方法
 * 因为在api-demo里依赖的utilsDemo的pom
 */
@RestController
@RequestMapping("/test")
public class TestController {
    /**
     * 测试获取时间
     */
    @GetMapping("/date")
    public String getDate(){
        Date d = TestDemo.getCurDate();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd:HH:mm");
    return "当前时间" + df.format(d);
    }
}
