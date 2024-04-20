package com.dudulu.seckill_my.vo;

import com.dudulu.seckill_my.pojo.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @program: SecKill_my
 * @description: 详情返回对象
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetailVo {
    private User user;
    private GoodsVo goodsVo;
    private int remainSeconds;
    private int secKillStatus;
}
