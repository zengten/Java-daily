package com.zt.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ZT
 * @version 1.0
 * @description: 测试freeMarker
 * @date 2023/2/27 20:51
 */
@Controller
public class TestController {

    /**
     * 测试
     */
    @GetMapping("test01")
    public ModelAndView test01() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("name", "zhangsan");
        modelAndView.setViewName("test01");
        return modelAndView;
    }

    /**
     * 测试类型数据显示
     *  布尔型：等价于 Java 的 Boolean 类型，不同的是不能直接输出，可转换为字符串输出
     *  ⽇期型：等价于 java 的 Date 类型，不同的是不能直接输出，需要转换成字符串再输出
     *  数值型：等价于 java 中的 int,float,double 等数值类型
     *  有三种显示形式：数值型(默认)、货币型、百分⽐型 字符型：等价于 java 中的字符串，有很多内置函数
     *  sequence 类型：等价于 java 中的数组，list，set 等集合类型
     *  hash 类型：等价于 java 中的 Map 类型
     */
    @GetMapping("test02")
    public ModelAndView test02() {
        ModelAndView modelAndView = new ModelAndView();
        Map<String, Object> map = new HashMap<>();
        map.put("flag", true);
        map.put("time", new Date());
        map.put("intValue", 6);
        map.put("doubleValue", 10.345f);
        map.put("avgValue", 0.1234);
        map.put("str", "Hello");
        modelAndView.addAllObjects(map);
        modelAndView.setViewName("test02");
        return modelAndView;
    }

}
