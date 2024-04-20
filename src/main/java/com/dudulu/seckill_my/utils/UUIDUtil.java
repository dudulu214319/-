package com.dudulu.seckill_my.utils;

import java.util.UUID;

/**
 * UUID工具类
 *
 * @ClassName: UUIDUtil
 */
public class UUIDUtil {

    public static String uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
