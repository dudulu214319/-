package com.dudulu.seckill_my.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dudulu.seckill_my.pojo.SeckillOrder;
import com.dudulu.seckill_my.pojo.User;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author dudulu
 */
public interface ISeckillOrderService extends IService<SeckillOrder> {

    Long getResult(User tUser, Long goodsId);
}
