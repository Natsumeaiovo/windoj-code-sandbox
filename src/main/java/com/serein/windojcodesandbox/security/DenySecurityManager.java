package com.serein.windojcodesandbox.security;

import java.security.Permission;

/**
 * @author: serein
 * @date: 2024/9/11 11:31
 * @description: 禁止所有权限的安全管理器
 */
public class DenySecurityManager extends SecurityManager {

    // 检查所有的权限
    @Override
    public void checkPermission(Permission perm) {
        throw new SecurityException("权限异常" + perm.getActions());
    }
}
