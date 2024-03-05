package com.dudulu.seckill_my.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.UpdateChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dudulu.seckill_my.exception.GlobalException;
import com.dudulu.seckill_my.mapper.OrderMapper;
import com.dudulu.seckill_my.pojo.Order;
import com.dudulu.seckill_my.pojo.SeckillGoods;
import com.dudulu.seckill_my.pojo.SeckillOrder;
import com.dudulu.seckill_my.pojo.User;
import com.dudulu.seckill_my.service.IGoodsService;
import com.dudulu.seckill_my.service.IOrderService;
import com.dudulu.seckill_my.service.ISeckillGoodsService;
import com.dudulu.seckill_my.service.ISeckillOrderService;
import com.dudulu.seckill_my.utils.MD5Utils;
import com.dudulu.seckill_my.utils.UUIDUtil;
import com.dudulu.seckill_my.vo.GoodsVo;
import com.dudulu.seckill_my.vo.OrderDetailVo;
import com.dudulu.seckill_my.vo.RespBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.StringUtils;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author dudulu
 * @since 2023-12-29
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {
    @Autowired
    private ISeckillGoodsService seckillGoodsService;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private ISeckillOrderService seckillOrderService;
    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private RedisTemplate redisTemplate;

    @Transactional
    @Override
    public Order seckill(User user, GoodsVo goodsVo) {
        // 减库存
        SeckillGoods seckillGoods = seckillGoodsService.getOne(new QueryWrapper<SeckillGoods>().eq("goods_id", goodsVo.getId()));
        seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);
        seckillGoodsService.updateById(seckillGoods);
//        boolean seckillGoodsResult = seckillGoodsService.update(new UpdateWrapper<SeckillGoods>()
//                .setSql("stock_count = " + "stock_count-1")
//                .eq("goods_id", goodsVo.getId())
//                .gt("stock_count", 0)
//        );
//        if(!seckillGoodsResult) {
//            return null;
//        }
        // 对最后一件物品的处理事务
        if (seckillGoods.getStockCount() < 1) {
            //判断是否还有库存
            redisTemplate.opsForValue().set("isStockEmpty:" + goodsVo.getId(), "0");
            return null;
        }
        // 创建订单
        Order order = new Order();
        order.setUserId(user.getId());
        order.setGoodsId(goodsVo.getId());
        order.setDeliveryAddrId(0L);
        order.setGoodsName(goodsVo.getGoodsName());
        order.setGoodsCount(1);
        order.setGoodsPrice(goodsVo.getGoodsPrice());
        order.setOrderChannel(1);
        order.setStatus(0);
        order.setCreateDate(new Date());
//        order.setPayDate(); // 未支付
        orderMapper.insert(order);
        // 创建秒杀订单
        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setUserId(user.getId());
        seckillOrder.setOrderId(order.getId()); // 外键
        seckillOrder.setGoodsId(goodsVo.getId());
        seckillOrderService.save(seckillOrder);

        // redis缓存seckillOrder (有个问题，redis重启的话，里面的缓存丢失，就会出现数据库里有seckillOrder但redis里没有)
        redisTemplate.opsForValue().set("order:"+user.getId()+":"+goodsVo.getId(), seckillOrder); // 缓存优化，没必要通过查找数据库判断有无重复的secOrder了

        return order;
    }

    @Override
    // 用来返回给点击秒杀后的订单详情界面orderDetail.htm界面
    public OrderDetailVo detail(Long orderId) throws GlobalException {
        if(orderId == null) {
           throw new GlobalException(RespBeanEnum.ORDER_NOT_EXIST);
        }
        Order order = orderMapper.selectById(orderId);
        GoodsVo goodsVo = goodsService.findGoodsVoById(order.getGoodsId());
        OrderDetailVo detailVo = new OrderDetailVo();
        detailVo.setOrder(order);
        detailVo.setGoodsVo(goodsVo);
        return detailVo;
    }

    @Override
    public String createPath(User user, Long goodsId) {
        String str = MD5Utils.md5(UUIDUtil.uuid() + "123456");
        redisTemplate.opsForValue().set("seckillPath:" + user.getId() + ":" + goodsId, str, 1, TimeUnit.MINUTES);
        return str;
    }

    @Override
    public boolean checkPath(User user, Long goodsId, String path) {
        if (user == null || goodsId < 0 || StringUtils.isEmpty(path)) {
            return false;
        }
        String redisPath = (String) redisTemplate.opsForValue().get("seckillPath:" + user.getId() + ":" + goodsId);
        return path.equals(redisPath);
    }

    @Override
    public boolean checkCaptcha(User user, Long goodsId, String captcha) {
        if (user == null || goodsId < 0 || StringUtils.isEmpty(captcha)) {
            return false;
        }
        String redisCaptcha = (String) redisTemplate.opsForValue().get("captcha:" + user.getId() + ":" + goodsId);
        return captcha.equals(redisCaptcha);
    }
}
