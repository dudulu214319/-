package com.dudulu.seckill_my.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @program: SecKill_my
 * @description: 秒杀信息
 * @author: Mr.Wang
 * @create: 2024-02-28 16:27
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeckillMessage {
    private User user;
    private Long goodsId;
}
