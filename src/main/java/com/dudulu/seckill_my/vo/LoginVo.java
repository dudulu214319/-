package com.dudulu.seckill_my.vo;

import com.dudulu.seckill_my.validator.IsMobile;
import lombok.Data;
import lombok.NonNull;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

/**
 * @program: SecKill_my
 * @description: 登录参数
 * @author: Mr.Wang
 * @create: 2023-12-09 16:44
 **/

@Data
public class LoginVo {
    @NotNull
    @IsMobile(required = true)
    private String mobile;
    @NotNull
    @Length(min = 32)
    private String password;
}
