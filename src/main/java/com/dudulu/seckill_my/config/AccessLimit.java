package com.dudulu.seckill_my.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AccessLimit { // 运行时动态处理，运行时通过代码里标识的元数据动态处理，例如使用反射注入实例。

    int second();

    int maxCount();

    boolean needLogin() default true;
}