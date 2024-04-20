package com.dudulu.seckill_my.controller;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.dudulu.seckill_my.pojo.User;
import com.dudulu.seckill_my.service.IGoodsService;
import com.dudulu.seckill_my.service.IUserService;
import com.dudulu.seckill_my.vo.DetailVo;
import com.dudulu.seckill_my.vo.GoodsVo;
import com.dudulu.seckill_my.vo.RespBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.Thymeleaf;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @program: SecKill_my
 * @description: 商品
 **/
@Controller
@RequestMapping("/goods")
public class GoodsController {
    @Autowired
    private IUserService userService;
    @Autowired
    private IGoodsService GoodsService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private ThymeleafViewResolver thymeleafViewResolver;

    // 需要先判断是否已经登陆过了，但这里我比较奇怪这三个参数是如何解析出来的。session是由服务端保存的，但浏览器也会有id什么的保存，可以说二者都可以感知session。(但session肯定是刚才在Userimpl里set的那个request.getSession())。
    // 如果找到了相应的 session 对象，则认为是之前标志过的一次会话，返回该 session 对象，数据达到共享。
    @RequestMapping(value = "/toList", produces = "text/html;charset=utf-8")
    @ResponseBody
    // 配置了WebMvcConfigurer，里面引入了一个UserArgumentResolver来拦截每次过来的request，读取里面的ticket来获取User
    public String toList(Model model, User user, HttpServletRequest request, HttpServletResponse response) {
        // Redis中获取页面，如果不为空，直接返回页面
        ValueOperations valueOperations = redisTemplate.opsForValue();
        String html = (String) valueOperations.get("goodsList");
        if(!StringUtils.isEmpty(html)) {
            return html;
        }
//        // 在一次会话（一个用户的多次请求）期间共享数据
//        User user = (User) session.getAttribute(ticket);
//        // 现在不用session中转保存数据，直接去redis里搞
//        User user = userService.getUserByCookie(ticket, request, response);
        if (user == null) {
            return "login";
        }
        model.addAttribute("user", user);
        model.addAttribute("goodsList", GoodsService.findGoodsVo()); // 返回GoodsVo列表
        // 如果为空，手动渲染，存入Redis渲染好的html并返回
        // 之前是springboot框架自己渲染html，这里自己调thymeleaf的引擎和webContext渲染
        WebContext webContext = new WebContext(request, response, request.getServletContext(), request.getLocale(), model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goodsList", webContext);
        if(!StringUtils.isEmpty(html)) {
            valueOperations.set("goodsList", html, 60, TimeUnit.SECONDS);
        }
        return html; // 不再走视图处理器
    }

    @RequestMapping(value = "/toDetail2/{goodsId}", produces = "text/html;charset=utf-8")
    @ResponseBody
    public String toDetail2(Model model, User user, @PathVariable Long goodsId, HttpServletRequest request, HttpServletResponse response) {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        String html = (String)valueOperations.get("goodsDetail:" + goodsId);
        if(!StringUtils.isEmpty(html)) {
            return html;
        }
//        if (user == null) {
//            return "login";
//        }
        model.addAttribute("user", user);
        GoodsVo goods = GoodsService.findGoodsVoById(goodsId);
        Date startDate = goods.getStartDate();
        Date endDate = goods.getEndDate();
        Date nowDate = new Date();
        // 秒杀状态
        int secKillStatus = 0;
        // 秒杀倒计时
        int remainSeconds = 0;
        if (nowDate.before(startDate)) {
            remainSeconds = (int)((startDate.getTime() - nowDate.getTime()) / 1000);
        }else if (nowDate.after(endDate)) {
            secKillStatus = 2;
            remainSeconds = -1;
        }else {
            secKillStatus = 1;
            remainSeconds = 0;
        }
        model.addAttribute("remainSeconds", remainSeconds);
        model.addAttribute("secKillStatus", secKillStatus);
        model.addAttribute("goods", goods); // 返回单个GoodsVo
        WebContext webContext = new WebContext(request, response, request.getServletContext(), request.getLocale(), model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goodsDetail", webContext);
        if(!StringUtils.isEmpty(html)) {
            valueOperations.set("goodsDetail:" + goodsId, html, 60, TimeUnit.SECONDS);
    }
        return html; // ViewResolver是Spring MVC里的东西
    }

    @RequestMapping(value = "/toDetail/{goodsId}") // @PathVariable用于获取路径参数，@RequestParam用于获取查询参数。
    @ResponseBody
    public RespBean toDetail(Model model, User user, @PathVariable Long goodsId, HttpServletRequest request, HttpServletResponse response) {
        GoodsVo goods = GoodsService.findGoodsVoById(goodsId);
        Date startDate = goods.getStartDate();
        Date endDate = goods.getEndDate();
        Date nowDate = new Date();
        // 秒杀状态
        int secKillStatus = 0;
        // 秒杀倒计时
        int remainSeconds = 0;
        if (nowDate.before(startDate)) {
            remainSeconds = (int)((startDate.getTime() - nowDate.getTime()) / 1000);
        }else if (nowDate.after(endDate)) {
            secKillStatus = 2;
            remainSeconds = -1;
        }else {
            secKillStatus = 1;
            remainSeconds = 0;
        }
        DetailVo detailVo = new DetailVo();
        detailVo.setUser(user);
        detailVo.setGoodsVo(goods);
        detailVo.setRemainSeconds(remainSeconds);
        detailVo.setSecKillStatus(secKillStatus);
        return RespBean.success(detailVo); // 只传数据不传html了
    }
}
