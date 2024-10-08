package com.serein.windojcodesandbox.security;

import java.security.Permission;

/**
 * @author: serein
 * @date: 2024/9/11 14:50
 * @description: 我的安全管理器
 */
public class MySecurityManager extends SecurityManager {

    // 检查所有的权限
    @Override
    public void checkPermission(Permission perm) {
//        super.checkPermission(perm);
    }

    // 检查程序是否可执行
    @Override
    public void checkExec(String cmd) {
        throw new SecurityException("checkExec 权限异常:" + cmd);
    }

    // 检查程序是否允许读文件
    @Override
    public void checkRead(String file) {
//        System.out.println(file);
//        throw new SecurityException("checkRead 权限异常" + file);
    }

    // 检查程序是否允许写文件
    @Override
    public void checkWrite(String file) {
//        throw new SecurityException("checkWrite 权限异常:" + file);
    }

    // 检查程序是否允许删文件
    @Override
    public void checkDelete(String file) {
//        throw new SecurityException("checkDelete 权限异常:" + file);
    }

    // 检查程序是否允许连接网络
    @Override
    public void checkConnect(String host, int port) {
//        throw new SecurityException("checkConnect 权限异常:" + host + ":" + port);
    }
}
