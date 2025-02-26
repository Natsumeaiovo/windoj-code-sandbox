package com.serein.windojcodesandbox;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.dfa.WordTree;
import com.serein.windojcodesandbox.model.ExecuteCodeRequest;
import com.serein.windojcodesandbox.model.ExecuteCodeResponse;
import com.serein.windojcodesandbox.model.ExecuteMessage;
import com.serein.windojcodesandbox.model.JudgeInfo;
import com.serein.windojcodesandbox.utils.ProcessUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Java 代码沙箱模板的实现
 */
@Slf4j
public abstract class JavaCodeSandboxTemplate implements CodeSandbox {

    private static final String GLOBAL_CODE_DIR_NAME = "tmpCode";
    private static final String GLOBAL_JAVA_CLASS_NAME = "Main.java";

    private static final long TIME_OUT = 5000L;

    private static final String SECURITY_MANAGER_PATH = "D:\\Project\\windoj-code-sandbox\\src\\main\\resources\\security";

    private static final String SECURITY_MANAGER_CLASS_NAME = "MySecurityManager";


    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        List<String> inputList = executeCodeRequest.getInputList();
        String code = executeCodeRequest.getCode();
        String language = executeCodeRequest.getLanguage();
        ExecuteCodeResponse executeCodeResponse = null;
        // 1. 把用户代码保存为文件
        File userCodeFile = saveCodeToFile(code);

        try {
            // 2. 编译代码，得到 class 文件
            ExecuteMessage compileFileExecuteMessage = compileFile(userCodeFile);
            System.out.println(compileFileExecuteMessage);

            // 3. 执行代码，得到输出结果
            List<ExecuteMessage> executeMessageList = runFile(userCodeFile, inputList);

            // 4. 整理输出，得到输出结果
            executeCodeResponse = getOutputResponse(executeMessageList);

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            // 5. 文件清理，防止服务器空间不足
            boolean b = deleteTmpCode(userCodeFile);
            if (!b) {
                log.error("删除代码文件异常， userCodeFilePath = {}", userCodeFile.getAbsolutePath());
            }
        }
        System.out.println("代码执行结果返回：" + executeCodeResponse);
        return executeCodeResponse;
    }

    /**
     * 1. 把用户代码保存为文件
     * @param code 用户代码
     * @return
     */
    public File saveCodeToFile(String code) {
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
        return userCodeFile;
    }

    /**
     * 2. 编译代码，得到 class 文件
     * @param userCodeFile
     * @return
     */
    public ExecuteMessage compileFile(File userCodeFile) {
        String compileCmd = String.format("javac -encoding utf-8 %s", userCodeFile.getAbsolutePath());
        try {
            Process compileProcess = Runtime.getRuntime().exec(compileCmd);
            ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(compileProcess, "编译");
            if (executeMessage.getExitValue() != 0) {
                throw new RuntimeException("编译错误");
            }
            return executeMessage;
        } catch (Exception e) {
            deleteTmpCode(userCodeFile);
//            return getErrorResponse(e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 3. 运行编译后的代码文件，获得执行结果列表
     * @param userCodeFile
     * @param inputList
     * @return
     */
    public List<ExecuteMessage> runFile(File userCodeFile, List<String> inputList) {
        List<ExecuteMessage> executeMessageList = new ArrayList<>();
        String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();    // root/tmpCode/uuid
        // 每个输入用例分别执行
        for (String inputArgs : inputList) {
            String runCmd = String.format("java -Dfile.encoding=UTF-8 -cp %s Main %s",
                    userCodeParentPath, inputArgs);
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
                throw new RuntimeException("编译后代码程序执行异常: ", e);
            }
        }
        return executeMessageList;
    }

    /**
     * 4. 整理输出，得到输出结果
     * @param executeMessageList
     * @return
     */
    public ExecuteCodeResponse getOutputResponse(List<ExecuteMessage> executeMessageList) {
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        List<String> outputList = new ArrayList<>();
        long maxExeTime = 0, maxExeMem = 0;
        String responseMessage = null;  // 用来记录执行错误信息
        for (ExecuteMessage executeMessage : executeMessageList) {
            Long time = executeMessage.getTime();
            if (time != null) {
                maxExeTime = Math.max(maxExeTime, time);    // 拿到所有输入用例中，执行的最大执行时间
            }
            String errorMessage = executeMessage.getErrorMessage();
            Long memory = executeMessage.getMemory();
            if (memory != null) {
                maxExeMem = Math.max(maxExeMem, memory);    // 拿到所有输入用例中，执行的最大执行内存
            }
            if (errorMessage != null) {
                // 执行中存在错误
                executeCodeResponse.setStatus(3);
                responseMessage = errorMessage;
            }
            outputList.add((executeMessage.getMessage()));  // 沙箱返回的executeMessage中的message就是程序的输出结果
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
        judgeInfo.setMemory(maxExeMem);
        executeCodeResponse.setJudgeInfo(judgeInfo);

        return executeCodeResponse;
    }

    /**
     * 5. 删除临时代码文件
     * @param userCodeFile
     */
    private boolean deleteTmpCode(File userCodeFile) {
        File parentFile = userCodeFile.getParentFile();
        if (parentFile != null) {
            boolean del = FileUtil.del(parentFile);
            System.out.println("服务器删除临时代码文件" + (del ? "成功" : "失败！"));
            return del;
        }
        return true;
    }


    /**
     * 6. 获取错误响应
     * @param e
     * @return
     */
    private ExecuteCodeResponse getErrorResponse(Throwable e) {
        // 6. 统一错误处理
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        executeCodeResponse.setOutputList(new ArrayList<>());
        executeCodeResponse.setMessage(e.getMessage());
        // 表示代码沙箱错误
        executeCodeResponse.setStatus(2);
        executeCodeResponse.setJudgeInfo(new JudgeInfo());
        return executeCodeResponse;
    }

}
