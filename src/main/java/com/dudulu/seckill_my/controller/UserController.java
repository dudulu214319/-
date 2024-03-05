package com.dudulu.seckill_my.controller;


import com.dudulu.seckill_my.pojo.User;
import com.dudulu.seckill_my.rabbitmq.MQSender;
import com.dudulu.seckill_my.vo.RespBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * <p>
 *  用户信息测试
 * </p>
 *
 * @author dudulu
 * @since 2023-11-21
 */
@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    private MQSender mqSender;

    @RequestMapping("/info")
    @ResponseBody
    public RespBean info(User user) {
        return RespBean.success(user);
    }

//    @RequestMapping("/mq/fanout")
//    @ResponseBody
//    public void mq(){
//        mqSender.send("Hello");
//    }
}
