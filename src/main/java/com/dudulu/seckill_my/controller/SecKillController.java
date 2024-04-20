package com.dudulu.seckill_my.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dudulu.seckill_my.config.AccessLimit;
import com.dudulu.seckill_my.exception.GlobalException;
import com.dudulu.seckill_my.pojo.Order;
import com.dudulu.seckill_my.pojo.SeckillMessage;
import com.dudulu.seckill_my.pojo.SeckillOrder;
import com.dudulu.seckill_my.pojo.User;
import com.dudulu.seckill_my.rabbitmq.MQSender;
import com.dudulu.seckill_my.service.IGoodsService;
import com.dudulu.seckill_my.service.IOrderService;
import com.dudulu.seckill_my.service.ISeckillOrderService;
import com.dudulu.seckill_my.utils.JsonUtil;
import com.dudulu.seckill_my.vo.GoodsVo;
import com.dudulu.seckill_my.vo.RespBean;
import com.dudulu.seckill_my.vo.RespBeanEnum;
import com.rabbitmq.tools.json.JSONUtil;
import com.wf.captcha.ArithmeticCaptcha;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @program: SecKill_my
 * @description: 实现秒杀（按钮点击）
 **/
@Slf4j
@Controller
@RequestMapping("/secKill")
public class SecKillController implements InitializingBean { // bean生命周期
    @Autowired
    private IGoodsService GoodsService;
    @Autowired
    private ISeckillOrderService seckillOrderService;
    @Autowired
    private IOrderService orderService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private MQSender mqSender;
    @Autowired
    private RedisScript<Long> redisScript;

    private Map<Long, Boolean> EmptyStockMap = new HashMap<>();

    @GetMapping(value = "/captcha")
    public void verifyCode(User tUser, Long goodsId, HttpServletResponse response) throws GlobalException {
        if (tUser == null || goodsId < 0) {
            throw new GlobalException(RespBeanEnum.REQUEST_ILLEGAL);
        }
        //设置响应头为输出图片的类型
        response.setContentType("image/jpg");
        response.setHeader("Pargam", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        //生成验证码
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(130, 32, 3);
        redisTemplate.opsForValue().set("captcha:" + tUser.getId() + ":" + goodsId, captcha.text(), 300, TimeUnit.SECONDS);
        try {
            captcha.out(response.getOutputStream());
        } catch (IOException e) {
            log.error("验证码生成失败", e.getMessage());
        }
    }

    @AccessLimit(second = 5, maxCount = 5, needLogin = true) // 通用处理：在应用程序中可能存在所有方法都要返回的信息，这是可以利用拦截器来实现，省去每个方法冗余重复的代码实现。
    @GetMapping(value = "/path")
    @ResponseBody
    public RespBean getPath(User user, Long goodsId, String captcha, HttpServletRequest request) {
        if (user == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        ValueOperations valueOperations = redisTemplate.opsForValue();
//      限制访问次数，5秒内访问5次
//      requestURI = /greenwx/testUrl
//      requestURL = http://127.0.0.1:8080/greenwx/testUrl
        String uri = request.getRequestURI();
//        captcha = "0";
        Integer count = (Integer) valueOperations.get(uri + ":" + user.getId());
        if (count == null) {
            valueOperations.set(uri + ":" + user.getId(), 1, 5, TimeUnit.SECONDS);
        } else if (count < 5) {
            valueOperations.increment(uri + ":" + user.getId());
        } else {
            return RespBean.error(RespBeanEnum.ACCESS_LIMIT_REACHED);
        }

        boolean check = orderService.checkCaptcha(user, goodsId, captcha);
        if (!check) {
            return RespBean.error(RespBeanEnum.ERROR_CAPTCHA);
        }
        String str = orderService.createPath(user, goodsId);
        return RespBean.success(str); // 动态字符串返回给前端
    }


    @GetMapping("/getResult")
    @ResponseBody
    // 客户端轮询
    public RespBean getResult(User user, Long goodsId) {
        if (user == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        Long orderId = seckillOrderService.getResult(user, goodsId);
        //        SeckillOrder seckillOrder = seckillOrderService.getOne(new QueryWrapper<SeckillOrder>().
//                eq("user_id", user.getId()).eq("goods_id", goodsId));
        return RespBean.success(orderId);
    }


    @RequestMapping("/doSecKill2") // 并发测试秒杀按钮，读取config.txt中的ticket让拦截器拿到user能过这一关让每个用户创建order和secOrder，goodsId我猜就是指定一个拿到goodsVo
    public String doSeckill2(Model model, User user, Long goodsId) {
        if(user == null) {
            return "login";
        }
        model.addAttribute("user", user);
        GoodsVo goodsVo = GoodsService.findGoodsVoById(goodsId);
        // 判断库存
        if(goodsVo.getStockCount() < 1) {
            model.addAttribute("errmsg", RespBeanEnum.EMPTY_STOCK.getMessage());
            return "secKillFail";
        }
        // 判断是否重复抢购
        SeckillOrder seckillOrder = seckillOrderService.getOne(new QueryWrapper<SeckillOrder>().
                eq("user_id", user.getId()).eq("goods_id", goodsId));
        if(seckillOrder != null) {
            model.addAttribute("errmsg", RespBeanEnum.REPEATE_ERROR.getMessage());
            return "secKillFail";
        }
        Order order = orderService.seckill(user, goodsVo); // 某用户秒杀某商品，创建order和secOrder
        model.addAttribute("order", order);
        model.addAttribute("goods", goodsVo);
        return "orderDetail";
    }

    @RequestMapping(value = "/{path}/doSecKill", method = RequestMethod.POST) // 并发测试秒杀按钮，读取config.txt中的ticket让拦截器拿到user能过这一关让每个用户创建order和secOrder，goodsId我猜就是指定一个拿到goodsVo
    @ResponseBody
    public RespBean doSeckill(@PathVariable String path, Model model, User user, Long goodsId) {
        if(user == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }

        ValueOperations valueOperations = redisTemplate.opsForValue();
        // 0.检查路径
        boolean check = orderService.checkPath(user, goodsId, path);
        if (!check) {
            return RespBean.error(RespBeanEnum.REQUEST_ILLEGAL);
        }

        // 1.重复抢购
        SeckillOrder seckillOrder = (SeckillOrder) valueOperations.get("order:" + user.getId() + ":" + goodsId);
        if(seckillOrder != null) {
            model.addAttribute("errmsg", RespBeanEnum.REPEATE_ERROR.getMessage());
            return RespBean.error(RespBeanEnum.REPEATE_ERROR);
        }
        // 1.5 内存标记，减少不必要的对redis的访问
        if(EmptyStockMap.get(goodsId)) {
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        // 2.redis预减库存，这里用lua脚本保证（并发）原子性，保证线程安全
//        Long stock = valueOperations.decrement("seckillGoods:" + goodsId);
        Long stock = (Long) redisTemplate.execute(redisScript, Collections.singletonList("seckillGoods:" + goodsId), Collections.EMPTY_LIST);
        if (stock < 0) {
            EmptyStockMap.put(goodsId, true);
            valueOperations.increment("seckillGoods:" + goodsId); // 加回0
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        // 3.构建seckillMessage，mqSender发送给queue
        SeckillMessage seckillMessage = new SeckillMessage(user, goodsId);
        mqSender.sendSeckillMessage(JsonUtil.object2JsonStr(seckillMessage)); // 异步发送json
        return RespBean.success(0); // 秒杀消息已发送给mq，正在排队

        /*
        model.addAttribute("user", user);
        GoodsVo goodsVo = GoodsService.findGoodsVoById(goodsId);
        // 判断库存
        if(goodsVo.getStockCount() < 1) {
            model.addAttribute("errmsg", RespBeanEnum.EMPTY_STOCK.getMessage());
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        // 判断是否重复抢购，不仅在数据库里存好了，还加了一份缓存，提高判断效率
//        SeckillOrder seckillOrder = seckillOrderService.getOne(new QueryWrapper<SeckillOrder>().
//                eq("user_id", user.getId()).eq("goods_id", goodsId));
        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsId);
        if(seckillOrder != null) {
            model.addAttribute("errmsg", RespBeanEnum.REPEATE_ERROR.getMessage());
            return RespBean.error(RespBeanEnum.REPEATE_ERROR);
        }
        Order order = orderService.seckill(user, goodsVo); // 某用户秒杀某商品，创建order和secOrder写入数据库
        model.addAttribute("order", order);
        model.addAttribute("goods", goodsVo);
        */
    }

    /**
     * 初始化的时候加载<goodId : 库存数量>到redis里 （缓存预热）
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> list = GoodsService.findGoodsVo();
        if(CollectionUtils.isEmpty(list)) {
            return;
        }
        list.forEach(goodsVo -> {
            redisTemplate.opsForValue().set("seckillGoods:" + goodsVo.getId(),goodsVo.getStockCount());
            EmptyStockMap.put(goodsVo.getId(), false); // 内存标记
        });
    }
}
