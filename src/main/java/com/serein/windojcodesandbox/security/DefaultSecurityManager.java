package com.serein.windojcodesandbox.security;

import java.security.Permission;

/**
 * @author: serein
 * @date: 2024/9/11 11:31
 * @description: 默认安全管理器
 */
public class DefaultSecurityManager extends SecurityManager {

    // 检查所有的权限
    @Override
    public void checkPermission(Permission perm) {
        System.out.println(perm);
        System.out.println("默认不做任何的权限限制");
    }
}
