package com.dudulu.seckill_my;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.dudulu.seckill_my.mapper")
public class SecKillMyApplication {
    public static void main(String[] args) {
        SpringApplication.run(SecKillMyApplication.class, args);
    }
}
