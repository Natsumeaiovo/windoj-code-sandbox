package com.serein.windojcodesandbox;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.dfa.FoundWord;
import cn.hutool.dfa.WordTree;
import com.serein.windojcodesandbox.model.ExecuteCodeRequest;
import com.serein.windojcodesandbox.model.ExecuteCodeResponse;
import com.serein.windojcodesandbox.model.ExecuteMessage;
import com.serein.windojcodesandbox.model.JudgeInfo;
import com.serein.windojcodesandbox.security.DefaultSecurityManager;
import com.serein.windojcodesandbox.security.DenySecurityManager;
import com.serein.windojcodesandbox.utils.ProcessUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author: serein
 * @date: 2024/9/9 14:58
 * @description:
 */
public class JavaNativeCodeSandbox implements CodeSandbox {

    private static final String GLOBAL_CODE_DIR_NAME = "tmpCode";
    private static final String GLOBAL_JAVA_CLASS_NAME = "Main.java";

    private static final List<String> blcakList = Arrays.asList("Files", "exec");

    private static final WordTree WORD_TREE;

    private static final long TIME_OUT = 5000L;

    private static final String SECURITY_MANAGER_PATH = "D:\\Project\\windoj-code-sandbox\\src\\main\\resources\\security";

    private static final String SECURITY_MANAGER_CLASS_NAME = "MySecurityManager";

    // 静态代码块，类被加载到JVM时自动执行，而无需创建类的实例，且只会执行一次。用于初始化静态变量，或者执行只需要在类加载时执行一次的操作。
    static {
        // 初始化字典树
        WORD_TREE = new WordTree();
        WORD_TREE.addWords(blcakList);
    }

    public static void main(String[] args) {
        JavaNativeCodeSandbox javaNativeCodeSandbox = new JavaNativeCodeSandbox();
        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
        executeCodeRequest.setInputList(Arrays.asList("1 2", "2 3"));
//        String code = ResourceUtil.readStr("testCode/unsafeCode/RunFileError.java", StandardCharsets.UTF_8);
//        String code = ResourceUtil.readStr("testCode/unsafeCode/WriteFileError.java", StandardCharsets.UTF_8);
//        String code = ResourceUtil.readStr("testCode/unsafeCode/FileLeakError.java", StandardCharsets.UTF_8);
//        String code = ResourceUtil.readStr("testCode/unsafeCode/MemoryError.java", StandardCharsets.UTF_8);
//        String code = ResourceUtil.readStr("testCode/unsafeCode/SleepError.java", StandardCharsets.UTF_8);
        String code = ResourceUtil.readStr("testCode/simpleComputeArgs/Main.java", StandardCharsets.UTF_8);
//        String code = ResourceUtil.readStr("testCode/simpleComputeScanner/Main.java", StandardCharsets.UTF_8);
        executeCodeRequest.setCode(code);
        executeCodeRequest.setLanguage("java");

        ExecuteCodeResponse executeCodeResponse = javaNativeCodeSandbox.executeCode(executeCodeRequest);
        System.out.println("执行代码响应：" + executeCodeResponse);
    }

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        List<String> inputList = executeCodeRequest.getInputList();
        String code = executeCodeRequest.getCode();
        String language = executeCodeRequest.getLanguage();
        // ------------------------------ 0. 校验代码是否包含黑名单命令 ------------------------------
//        FoundWord foundWord = WORD_TREE.matchWord(code);
//        if (foundWord != null) {
//            return ExecuteCodeResponse.builder()
//                    .status(3)
//                    .message("危险操作")
//                    .build();
//        }

        // ------------------------------  1. 把用户的代码保存为文件  ------------------------------
        String userDir = System.getProperty("user.dir");    // root
        // System.out.println("userDir: " + userDir);
        String globalCodePathName = userDir + File.separator + GLOBAL_CODE_DIR_NAME;    // root/tmpCode
        // 判断全局代码目录是否存在
        if (!FileUtil.exist(globalCodePathName)) {
            FileUtil.mkdir(globalCodePathName);
        }
        // 把用户代码隔离存放
        String userCodeParentPath = globalCodePathName + File.separator + UUID.randomUUID();    // root/tmpCode/uuid
        String userCodePath = userCodeParentPath + File.separator + GLOBAL_JAVA_CLASS_NAME;     // root/tmpCode/uuid/Main.java
        File userCodeFile = FileUtil.writeString(code, userCodePath, StandardCharsets.UTF_8);

        // ------------------------------  2. 编译代码，得到 class 文件  ------------------------------
        String compileCmd = String.format("javac -encoding utf-8 %s", userCodeFile.getAbsolutePath());
        try {
            Process compileProcess = Runtime.getRuntime().exec(compileCmd);
            ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(compileProcess, "编译");
            System.out.println(executeMessage);
        } catch (Exception e) {
            deleteTmpCode(userCodeFile);
            return getErrorResponse(e);
        }

        // ------------------------------  3. 执行代码，得到输出结果  ------------------------------
        List<ExecuteMessage> executeMessageList = new ArrayList<>();
        // 每个输入用例分别执行
        for (String inputArgs : inputList) {
            String runCmd = String.format("java -Dfile.encoding=UTF-8 -cp %s;%s -Djava.security.manager=%s Main %s",
                    userCodeParentPath, SECURITY_MANAGER_PATH, SECURITY_MANAGER_CLASS_NAME, inputArgs);
            try {
                Process runProcess = Runtime.getRuntime().exec(runCmd);
                // 超时控制，创建一个守护线程，超时后自动中断 process
                // 创建一个原子性boolean变量，是线程安全的，可以在多个线程间安全操作该变量
                AtomicBoolean timeout = new AtomicBoolean(false);
                new Thread(() -> {
                    try {
                        Thread.sleep(TIME_OUT);
                        runProcess.destroy();
                        timeout.set(true);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }).start();
//                ExecuteMessage executeMessage = ProcessUtils.runInteractProcessAndGetMessage(runProcess, "运行", inputArgs);
                ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(runProcess, "运行");
                if (timeout.get()) {
                    executeMessage.setErrorMessage("超时");
                }
                System.out.println(executeMessage);
                executeMessageList.add(executeMessage); // 将程序运行信息添加到 list
            } catch (Exception e) {
                deleteTmpCode(userCodeFile);
                return getErrorResponse(e);
            }
        }

        // ------------------------------  4. 整理输出，得到输出结果  ------------------------------
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        List<String> outputList = new ArrayList<>();
        long maxExeTime = 0;
        String responseMessage = null;
        for (ExecuteMessage executeMessage : executeMessageList) {
            Long time = executeMessage.getTime();
            if (time != null) {
                maxExeTime = Math.max(maxExeTime, time);    // 拿到所有输入用例中，执行的最大执行时间
            }
            String errorMessage = executeMessage.getErrorMessage();
            if (errorMessage != null) {
                // 执行中存在错误
                executeCodeResponse.setStatus(3);
                responseMessage = errorMessage;
            }
            outputList.add((executeMessage.getMessage()));  // 沙箱返回的executeMessage就是程序的输出结果
        }
        executeCodeResponse.setMessage(responseMessage);   // 设置错误信息
        executeCodeResponse.setOutputList(outputList);
        // 如果正常执行完成
        if (executeCodeResponse.getStatus() == null) {
            executeCodeResponse.setStatus(1);
            executeCodeResponse.setMessage("ok");
        }

        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setTime(maxExeTime);
//        todo judgeInfo.setMemory();
        executeCodeResponse.setJudgeInfo(judgeInfo);

        // ------------------------------  5. 文件清理，防止服务器空间不足  ------------------------------
        deleteTmpCode(userCodeFile);

        return executeCodeResponse;
    }

    /**
     * 删除临时代码文件
     * @param userCodeFile
     */
    private void deleteTmpCode(File userCodeFile) {
        File parentFile = userCodeFile.getParentFile();
        if (parentFile != null) {
            boolean del = FileUtil.del(parentFile);
            System.out.println("服务器删除临时代码文件" + (del ? "成功" : "失败！"));
        }
    }

    /**
     * 获取错误响应
     * @param e
     * @return
     */
    private ExecuteCodeResponse getErrorResponse(Throwable e) {
        // ------------------------------  6. 统一错误处理  ------------------------------
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        executeCodeResponse.setOutputList(new ArrayList<>());
        executeCodeResponse.setMessage(e.getMessage());
        // 表示代码沙箱错误
        executeCodeResponse.setStatus(2);
        executeCodeResponse.setJudgeInfo(new JudgeInfo());
        return executeCodeResponse;
    }

}
