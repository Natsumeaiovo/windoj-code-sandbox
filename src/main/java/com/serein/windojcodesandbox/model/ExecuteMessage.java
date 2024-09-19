package com.serein.windojcodesandbox.model;

import lombok.Data;

/**
 * @author: serein
 * @date: 2024/9/9 17:08
 * @description: 进程执行信息
 */
@Data
public class ExecuteMessage {
    /**
     * 程序退出代码
     */
    private Integer exitValue;

    /**
     * 程序执行消息
     */
    private String message;

    /**
     * 程序执行错误消息
     */
    private String errorMessage;

    /**
     * 程序执行用时
     */
    private Long time;

    /**
     * 程序执行占用内存
     */
    private Long memory;
}
