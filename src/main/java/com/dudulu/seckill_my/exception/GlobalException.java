package com.dudulu.seckill_my.exception;

import com.dudulu.seckill_my.vo.RespBeanEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @program: SecKill_my
 * @description: 全局异常
 * @author: Mr.Wang
 * @create: 2023-12-12 10:47
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GlobalException extends Exception{
    private RespBeanEnum respBeanEnum;
}

