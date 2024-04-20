package com.dudulu.seckill_my.rabbitmq;

import com.dudulu.seckill_my.pojo.Order;
import com.dudulu.seckill_my.pojo.SeckillMessage;
import com.dudulu.seckill_my.pojo.SeckillOrder;
import com.dudulu.seckill_my.pojo.User;
import com.dudulu.seckill_my.service.IGoodsService;
import com.dudulu.seckill_my.service.IOrderService;
import com.dudulu.seckill_my.utils.JsonUtil;
import com.dudulu.seckill_my.vo.GoodsVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 消息消费者
 *
 * @ClassName: MQReceiver
 */
@Service
@Slf4j
public class MQReceiver { // 没有注册在别的Contrtoller里过

    @Autowired
    private IGoodsService GoodsServicel;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private IOrderService OrderService;

    /**
     * 下单操作
     *
     * @param
     * @return void
     * @author LiChao
     * @operation add
     * @date 6:48 下午 2022/3/8
     **/
    @RabbitListener(queues = "seckillQueue")
    public void receive(String message) {
        log.info("接收消息：" + message);
        SeckillMessage seckillMessage = JsonUtil.jsonStr2Object(message, SeckillMessage.class); // message是json用反射拿到对象
        Long goodsId = seckillMessage.getGoodsId();
        User user = seckillMessage.getUser();
        GoodsVo goodsVo = GoodsServicel.findGoodsVoById(goodsId); // 异步检测
        if (goodsVo.getStockCount() < 1) { // 为了安全再判断一下库存
            return;
        }
        // 判断是否重复抢购
        SeckillOrder SeckillOrder = (SeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsId);  // 异步检测
        if (SeckillOrder != null) {
            return;
        }
        // 3.秒杀成功，完成【1.mysql数据库库存减一 2.创建订单 3.创建秒杀订单】事务
        // 疑问？OrderService不是代理对象，能启动事务吗？
        OrderService.seckill(user, goodsVo); // 某用户秒杀某商品，创建order和secOrder写入数据库
    }
}
