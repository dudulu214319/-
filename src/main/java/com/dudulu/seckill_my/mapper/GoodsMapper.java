package com.dudulu.seckill_my.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dudulu.seckill_my.pojo.Goods;
import com.dudulu.seckill_my.vo.GoodsVo;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author dudulu
 * @since 2023-12-29
 */
public interface GoodsMapper extends BaseMapper<Goods> {
    /**
     * 获取商品
     * @return
     */
    List<GoodsVo> findGoodsVo();

    GoodsVo findGoodsVoById(Long goodsId);
}
