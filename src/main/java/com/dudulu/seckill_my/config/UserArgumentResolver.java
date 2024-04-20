package com.dudulu.seckill_my.config;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.dudulu.seckill_my.pojo.User;
import com.dudulu.seckill_my.service.IUserService;
import com.dudulu.seckill_my.utils.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @program: SecKill_my
 * @description: 自定义用户参数
 **/
// 表示一个带注释的类是一个“组件”，成为Spring管理的Bean。当使用基于注解的配置和类路径扫描时，这些类被视为自动检测的候选对象。
@Component
public class UserArgumentResolver implements HandlerMethodArgumentResolver {
    @Autowired
    // 注入一个java bean
    private IUserService userService;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        Class<?> clazz = parameter.getParameterType();
        return clazz == User.class;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        return UserContext.getUser(); // 不用再取了，直接拿就行。拿的操作在Interceptor里做了，覆盖了原ArgumentResolver的操作。
        //        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
//        HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);
//        String ticket = CookieUtil.getCookieValue(request, "userTicket");
//        if(StringUtils.isEmpty(ticket)) {
//            return null;
//        }
//        return userService.getUserByCookie(ticket, request, response);
    }
}
