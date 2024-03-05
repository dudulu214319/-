package com.dudulu.seckill_my.vo;

import com.dudulu.seckill_my.pojo.Goods;
import com.dudulu.seckill_my.pojo.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @program: SecKill_my
 * @description: 订单细节
 * @author: Mr.Wang
 * @create: 2024-02-01 17:12
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailVo {
    private Order order;
    private GoodsVo goodsVo;
}
