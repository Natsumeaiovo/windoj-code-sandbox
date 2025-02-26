package com.serein.windojcodesandbox.controller;

import com.serein.windojcodesandbox.JavaDockerCodeSandbox;
import com.serein.windojcodesandbox.JavaNativeCodeSandbox;
import com.serein.windojcodesandbox.model.ExecuteCodeRequest;
import com.serein.windojcodesandbox.model.ExecuteCodeResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author: serein
 * @date: 2024/9/6 16:42
 * @description: 测试接口
 */
@RestController
@Slf4j
public class MainController {

    @Resource
    private JavaNativeCodeSandbox javaNativeCodeSandbox;

    @Resource
    private JavaDockerCodeSandbox javaDockerCodeSandbox;

    @GetMapping("/health")
    public String healthCheck() {
        return "ok";
    }

    /**
     * 沙箱执行代码
     * @param executeCodeRequest
     * @return
     */
    @PostMapping("/executeCode")
    public ExecuteCodeResponse executeCode(@RequestBody ExecuteCodeRequest executeCodeRequest) {
        log.info("执行代码请求：{}", executeCodeRequest);
        if (executeCodeRequest == null) {
            throw new RuntimeException("请求参数为空");
        }
        return javaNativeCodeSandbox.executeCode(executeCodeRequest);
//        return javaDockerCodeSandbox.executeCode(executeCodeRequest);
    }
}
