package com.serein.windojcodesandbox.unsafe;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author: serein
 * @date: 2024/9/10 16:29
 * @description: 运行其他程序（比如危险木马）
 */
public class RunFileError {

    public static void main(String[] args) throws IOException, InterruptedException {
        String userDir = System.getProperty("user.dir");
        String privateFilePath = userDir + File.separator + "src/main/resources/木马程序.bat";
        Process process = Runtime.getRuntime().exec(privateFilePath);
        process.waitFor();
        // 获得进程的输出
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String outputLine;
        while ((outputLine = bufferedReader.readLine()) != null) {
            System.out.println(outputLine);
        }
        System.out.println("执行异常程序成功");
    }
}
