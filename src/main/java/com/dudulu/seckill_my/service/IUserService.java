package com.dudulu.seckill_my.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dudulu.seckill_my.exception.GlobalException;
import com.dudulu.seckill_my.pojo.User;
import com.dudulu.seckill_my.vo.LoginVo;
import com.dudulu.seckill_my.vo.RespBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author dudulu
 */
public interface IUserService extends IService<User> {
    RespBean doLogin(LoginVo loginVo, HttpServletRequest request, HttpServletResponse response) throws GlobalException;
    User getUserByCookie(String userTicket, HttpServletRequest request, HttpServletResponse response);
    RespBean updatePassword(String userTicket, String password, HttpServletRequest request, HttpServletResponse response) throws GlobalException;
}
