package com.serein.windojcodesandbox.utils;

import cn.hutool.core.util.StrUtil;
import com.serein.windojcodesandbox.model.ExecuteMessage;
import org.springframework.util.StopWatch;

import java.io.*;

/**
 * @author: serein
 * @date: 2024/9/9 17:07
 * @description: 进程工具类
 */
public class ProcessUtils {

    /**
     * 执行进程并获取信息
     *
     * @param process
     * @return
     */
    public static ExecuteMessage runProcessAndGetMessage(Process process, String opName) {
        ExecuteMessage executeMessage = new ExecuteMessage();
        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            // 等待程序执行，获取错误码
            int exitValue = process.waitFor();
            executeMessage.setExitValue(exitValue);
            // 正常退出
            if (exitValue == 0) {
                System.out.println(opName + "成功: ");
                // 分批获取进程的正常输出
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                StringBuilder outputStringBuilder = new StringBuilder();
                // 逐行读取
                String outputLine;
                while ((outputLine = bufferedReader.readLine()) != null) {
                    outputStringBuilder.append(outputLine).append("\n");
                }
                executeMessage.setMessage(outputStringBuilder.toString());
            } else {
                // 异常退出
                System.out.println(opName + "失败，错误码: " + exitValue);
                // 分批获取进程的正常输出
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                StringBuilder outputStringBuilder = new StringBuilder();
                // 逐行读取
                String outputLine;
                while ((outputLine = bufferedReader.readLine()) != null) {
                    outputStringBuilder.append(outputLine).append("\n");
                }
                executeMessage.setMessage(outputStringBuilder.toString());

                // 分批获取进程的错误输出
                BufferedReader errorBufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                StringBuilder errorOutputStringBuilder = new StringBuilder();
                // 逐行读取
                String errorOutputLine;
                while ((errorOutputLine = errorBufferedReader.readLine()) != null) {
                    errorOutputStringBuilder.append(errorOutputLine).append("\n");
                }
                executeMessage.setErrorMessage(errorOutputStringBuilder.toString());
            }
            stopWatch.stop();
            executeMessage.setTime(stopWatch.getLastTaskTimeMillis());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return executeMessage;
    }

    /**
     * 执行交互式进程并获取信息
     *
     * @param process
     * @param opName
     * @return
     */
    public static ExecuteMessage runInteractProcessAndGetMessage(Process process, String opName, String args) {
        ExecuteMessage executeMessage = new ExecuteMessage();
        try {
            // 向控制台输入程序
            OutputStream outputStream = process.getOutputStream();
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
            String[] s = args.split(" ");
            String join = StrUtil.join("\n", s) + "\n";
            outputStreamWriter.write(join);
            // 相当于按下回车，执行输入的发送
            outputStreamWriter.flush();

            // 分批获取进程的正常输出
            InputStream inputStream = process.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder outputStringBuilder = new StringBuilder();
            // 逐行读取
            String outputLine;
            while ((outputLine = bufferedReader.readLine()) != null) {
                outputStringBuilder.append(outputLine);
            }
            executeMessage.setMessage(outputStringBuilder.toString());
            // 记得资源的释放，否则会卡死
            outputStreamWriter.close();
            inputStream.close();
            process.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return executeMessage;
    }
}
