package com.serein.windojcodesandbox.security;

import cn.hutool.core.io.FileUtil;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author: serein
 * @date: 2024/9/11 14:55
 * @description:
 */
public class TestSecurityManager {

    public static void main(String[] args) {
        System.setSecurityManager(new MySecurityManager());

//        List<String> strings = FileUtil.readLines("D:\\Project\\windoj-code-sandbox\\src\\main\\resources\\application.yml", StandardCharsets.UTF_8);
        FileUtil.writeString("aa", "aaa", StandardCharsets.UTF_8);
    }
}
