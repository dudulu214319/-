package com.dudulu.seckill_my.controller;


import com.dudulu.seckill_my.exception.GlobalException;
import com.dudulu.seckill_my.pojo.User;
import com.dudulu.seckill_my.service.IOrderService;
import com.dudulu.seckill_my.vo.OrderDetailVo;
import com.dudulu.seckill_my.vo.RespBean;
import com.dudulu.seckill_my.vo.RespBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author dudulu
 * @since 2023-12-29
 */
@Controller
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private IOrderService orderService;

    @RequestMapping("/detail")
    @ResponseBody
    public RespBean detail(User user, Long orderId) throws GlobalException {
        if(user == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        OrderDetailVo detailVo = orderService.detail(orderId);
        return RespBean.success(detailVo);
    }
}
