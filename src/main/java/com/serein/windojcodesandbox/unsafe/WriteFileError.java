package com.serein.windojcodesandbox.unsafe;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;

/**
 * @author: serein
 * @date: 2024/9/10 16:16
 * @description: 写文件，植入木马或危险程序
 */
public class WriteFileError {

    public static void main(String[] args) throws IOException {
        String userDir = System.getProperty("user.dir");
        String privateFilePath = userDir + File.separator + "src/main/resources/木马程序.bat";
        String errorProgram = "java -version 2>&1";
        Files.write(Paths.get(privateFilePath), Collections.singletonList(errorProgram));
        System.out.println("写入木马成功 :)");
    }
}
