package com.dudulu.seckill_my.exception;

import com.dudulu.seckill_my.vo.RespBeanEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @program: SecKill_my
 * @description: 全局异常
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GlobalException extends Exception{
    private RespBeanEnum respBeanEnum;
}

