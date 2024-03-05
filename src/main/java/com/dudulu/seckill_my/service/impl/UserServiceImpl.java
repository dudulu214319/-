package com.dudulu.seckill_my.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dudulu.seckill_my.exception.GlobalException;
import com.dudulu.seckill_my.mapper.UserMapper;
import com.dudulu.seckill_my.pojo.User;
import com.dudulu.seckill_my.service.IUserService;
import com.dudulu.seckill_my.utils.CookieUtil;
import com.dudulu.seckill_my.utils.MD5Utils;
import com.dudulu.seckill_my.utils.UUIDUtil;
import com.dudulu.seckill_my.vo.LoginVo;
import com.dudulu.seckill_my.vo.RespBean;
import com.dudulu.seckill_my.vo.RespBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author dudulu
 * @since 2023-11-21
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public RespBean doLogin(LoginVo loginVo, HttpServletRequest request, HttpServletResponse response) throws GlobalException {
        String mobile = loginVo.getMobile();
        String password = loginVo.getPassword();
//        if(StringUtils.isEmpty(mobile) || StringUtils.isEmptsy(password)) {
//            return RespBean.error(RespBeanEnum.LOGIN_ERROR);
//        }
//        if(!ValidatorUtil.isMobile(mobile)) {
//            return RespBean.error(RespBeanEnum.MOBILE_ERROR);
//        }
        // 一步完成数据库的连接和查询,userMapper里没有自定义实现什么东西
        User user = userMapper.selectById(mobile);
        if(user == null) {
//            return RespBean.error(RespBeanEnum.LOGIN_ERROR);
            throw new GlobalException(RespBeanEnum.LOGIN_ERROR);
        }
        if(!MD5Utils.formPassToDBPass(password, user.getSalt()).equals(user.getPassword())) {
//            return RespBean.error(RespBeanEnum.LOGIN_ERROR);
            throw new GlobalException(RespBeanEnum.LOGIN_ERROR);
        }
        // 生成cookie
        String ticket = UUIDUtil.uuid();
        //  session是由服务器创建的，并保存在服务器上的
        //  将用户信息直接存入redis（连接工厂里其实已经关联了redis的配置？），跳过了用session中转
        //  这里有些奇怪，完整流程应该是引入RedisConfig里的模板函数，传入RedisConnectionFactory构造出RedisTemplate<String, Object>，再使用
        redisTemplate.opsForValue().set("user:" + ticket, user); // String, object
//        request.getSession().setAttribute(ticket, user); // 在session里让cookie映射上用户
        //  这里传request是为了获取服务器的域名，解析出来然后放到cookie里 cookie.setDomain(domainName);
        CookieUtil.setCookie(request, response, "userTicket", ticket); // 给response设置上cookie传回给浏览器保存
        return RespBean.success(ticket);
    }

    @Override
    public User getUserByCookie(String userTicket, HttpServletRequest request, HttpServletResponse response) {
        if(userTicket.isEmpty()) {
            return null;
        }
        User user = (User) redisTemplate.opsForValue().get("user:" + userTicket);
        if(user!=null) {
            CookieUtil.setCookie(request, response, "userTicket", userTicket); // response.addCookie(cookie); 后续的每次response都给你带上cookie
        }
        return user;
    }

    @Override
    public RespBean updatePassword(String userTicket, String password, HttpServletRequest request, HttpServletResponse response) throws GlobalException {
        User user = getUserByCookie(userTicket, request, response);
        if (user == null) {
            throw new GlobalException(RespBeanEnum.MOBILE_NOT_EXIST);
        }
        user.setPassword(MD5Utils.inputPassToDBPass(password, user.getSalt()));
        int result = userMapper.updateById(user);
        if(result == 1) {
            // 数据库更新的时候删除redis
            redisTemplate.delete("user:"+userTicket);
            return RespBean.success();
        }
        return RespBean.error(RespBeanEnum.PASSWORD_UPDATE_FAIL);
    }
}