package com.dudulu.seckill_my.config;

import com.dudulu.seckill_my.pojo.User;

/**
 * @ClassName: UserContext
 */
public class UserContext {

    private static ThreadLocal<User> userThreadLocal = new ThreadLocal<>(); // User是Object类型

    public static void setUser(User tUser) {
        userThreadLocal.set(tUser);
    } // 操作map设置value

    public static User getUser() {
        return userThreadLocal.get(); // key不用管理，就是threadLocal对象
    }
}

