package com.dudulu.seckill_my.config;

import com.dudulu.seckill_my.pojo.User;
import com.dudulu.seckill_my.service.IUserService;
import com.dudulu.seckill_my.utils.CookieUtil;
import com.dudulu.seckill_my.vo.RespBean;
import com.dudulu.seckill_my.vo.RespBeanEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor; // org.springframework.web.servlet。该接口定义了三个方法，一个在执行实际处理程序之前调用，一个在处理程序执行后调用，另一个在完整请求完成后调用。这三种方法应该提供足够的灵活性来进行各种预处理和后处理。
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName: AccessLimitInterceptor
 */
@Component
public class AccessLimitInterceptor implements HandlerInterceptor { // 在action的生命周期中，拦截器可以多次被调用，而过滤器只能在容器初始化时被调用一次。

    @Autowired
    private IUserService itUserService; // 拦截器可以获取IOC容器中的各个bean，而过滤器就不行，这点很重要，在拦截器里注入一个service，可以调用业务逻辑。
    @Autowired
    private RedisTemplate redisTemplate;

    @Override // 在 Controoler 处理请求之前被调用，返回值是 boolean类型，如果是true就进行下一步操作；若返回false，则证明不符合拦截条件，在失败的时候不会包含任何响应，此时需要调用对应的response返回对应响应。
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            User tUser = getUser(request, response);
            UserContext.setUser(tUser); // 用threadLocal存在线程本地变量里，该线程可以在任意时刻、任意方法中获取缓存
            HandlerMethod hm = (HandlerMethod) handler;
            AccessLimit accessLimit = hm.getMethodAnnotation(AccessLimit.class); // 能get到注解的时候就说明有限流注解，进行限流
            if (accessLimit == null) {
                return true; // getpath()没有限流注解，放行
            }
            int second = accessLimit.second();
            int maxCount = accessLimit.maxCount();
            boolean needLogin = accessLimit.needLogin();

            String key = request.getRequestURI();
            if (needLogin) {
                if (tUser == null) {
                    render(response, RespBeanEnum.SESSION_ERROR);
                }
                key += ":" + tUser.getId();
            }
            ValueOperations valueOperations = redisTemplate.opsForValue();
            Integer count = (Integer) valueOperations.get(key);
            if (count == null) {
                valueOperations.set(key, 1, second, TimeUnit.SECONDS); // 设置了redis TTL
            } else if (count < maxCount) {
                valueOperations.increment(key); // 原子操作
            } else {
                render(response, RespBeanEnum.ACCESS_LIMIT_REACHED);
                return false; // 访问限制次数达到，拦截（对单一用户限流）
            }
        }
        return true; // 通过
    }

    private void render(HttpServletResponse response, RespBeanEnum respBeanEnum) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        PrintWriter printWriter = response.getWriter();
        RespBean bean = RespBean.error(respBeanEnum);
        printWriter.write(new ObjectMapper().writeValueAsString(bean));
        printWriter.flush();
        printWriter.close();
    }

    private User getUser(HttpServletRequest request, HttpServletResponse response) {
        String userTicket = CookieUtil.getCookieValue(request, "userTicket");
        if (StringUtils.isEmpty(userTicket)) {
            return null;
        }
        return itUserService.getUserByCookie(userTicket, request, response);
    }
}
