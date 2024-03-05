package com.dudulu.seckill_my.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dudulu.seckill_my.exception.GlobalException;
import com.dudulu.seckill_my.pojo.Order;
import com.dudulu.seckill_my.pojo.User;
import com.dudulu.seckill_my.vo.GoodsVo;
import com.dudulu.seckill_my.vo.OrderDetailVo;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author dudulu
 * @since 2023-12-29
 */
public interface IOrderService extends IService<Order> {

    Order seckill(User user, GoodsVo goodsVo); // 点击秒杀创建订单

    OrderDetailVo detail(Long orderId) throws GlobalException; // 根据orderId查询订单

    String createPath(User user, Long goodsId);

    boolean checkPath(User user, Long goodsId, String path);

    boolean checkCaptcha(User user, Long goodsId, String captcha);
}
