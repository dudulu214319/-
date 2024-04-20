package com.dudulu.seckill_my.controller;

import com.dudulu.seckill_my.exception.GlobalException;
import com.dudulu.seckill_my.service.IUserService;
import com.dudulu.seckill_my.vo.LoginVo;
import com.dudulu.seckill_my.vo.RespBean;
import io.swagger.annotations.ApiOperation;
import jdk.nashorn.internal.runtime.regexp.joni.ast.StringNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

/**
* @program: SecKill_my
* @description: 登录
**/
@Controller
@RequestMapping("/login")
@Slf4j
public class LoginController {

    @Autowired
    private IUserService userService;

    /**
     * 功能：跳转登陆界面
     * @return
     */
    @ApiOperation("跳转登录页面")
    @RequestMapping("/toLogin")
    public String toLogin(){
        return "login";
    }

    /**
     * 登录功能
     * @param loginVo
     * @return
     */
    @ApiOperation("登录接口")
    @RequestMapping("/doLogin")
    @ResponseBody // @ResponseBody的作用其实是将java对象转为json格式的数据。
    // @responseBody注解的作用是将controller的方法返回的对象通过适当的转换器转换为指定的格式之后，写入到response对象的body区，通常用来返回JSON数据或者是XML数据。
    // @ResponseBody 表示该方法的返回结果直接写入 HTTP response body 中，一般在异步获取数据时使用【也就是AJAX】。
    public RespBean doLogin(@Valid LoginVo loginVo, HttpServletRequest request, HttpServletResponse response) throws GlobalException {
//        log.info("{}", loginVo);
        return userService.doLogin(loginVo, request, response);
    }
}
