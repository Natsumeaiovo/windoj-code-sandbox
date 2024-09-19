package com.serein.windojcodesandbox.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: serein
 * @date: 2024/9/6 16:42
 * @description: 测试接口
 */
@RestController
public class MainController {

    @GetMapping("/health")
    public String healthCheck() {
        return "ok";
    }
}
