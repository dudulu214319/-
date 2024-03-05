package com.dudulu.seckill_my.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dudulu.seckill_my.pojo.Goods;
import com.dudulu.seckill_my.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author dudulu
 * @since 2023-12-29
 */
public interface IGoodsService extends IService<Goods> {
    List<GoodsVo> findGoodsVo();

    GoodsVo findGoodsVoById(Long goodsId);
}
