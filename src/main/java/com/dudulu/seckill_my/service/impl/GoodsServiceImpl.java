package com.dudulu.seckill_my.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dudulu.seckill_my.mapper.GoodsMapper;
import com.dudulu.seckill_my.pojo.Goods;
import com.dudulu.seckill_my.service.IGoodsService;
import com.dudulu.seckill_my.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author dudulu
 * @since 2023-12-29
 */
@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements IGoodsService {
    @Autowired
    private GoodsMapper goodsMapper;

    /**
     * 获取商品列表
     * @return
     */
    @Override
    public List<GoodsVo> findGoodsVo() {
        return goodsMapper.findGoodsVo();
    }

    @Override
    public GoodsVo findGoodsVoById(Long goodsId) {
        return goodsMapper.findGoodsVoById(goodsId);
    }
}
