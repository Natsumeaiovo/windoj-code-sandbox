package com.serein.windojcodesandbox.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author: serein
 * @date: 2024/9/5 11:20
 * @description: 代码执行请求类
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteCodeRequest {

    /**
     * 题目的一组输入用例
     */
    private List<String> inputList;

    /**
     * 要执行的代码
     */
    private String code;

    /**
     * 代码语言
     */
    private String language;
}
