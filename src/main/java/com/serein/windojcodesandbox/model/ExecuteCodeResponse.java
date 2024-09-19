package com.serein.windojcodesandbox.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author: serein
 * @date: 2024/9/5 11:20
 * @description: 代码执行相应类
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteCodeResponse {

    /**
     * 一组输入用例执行完代码 的 一组输出
     */
    private List<String> outputList;

    /**
     * 执行信息
     */
    private String message;

    /**
     * 执行状态
     */
    private Integer status;

    /**
     * 执行信息
     */
    private JudgeInfo judgeInfo;
}
