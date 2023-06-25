package com.nplat.convert.config;

import com.nplat.convert.entity.entity.CacheLocation;
import com.nplat.convert.entity.entity.User;
import com.nplat.convert.mapper.UserMapper;
import com.nplat.convert.service.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;


@Slf4j
@Component
public class WxUserHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private CacheService cacheService;

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        return methodParameter.getParameterType().isAssignableFrom(User.class) &&
                methodParameter.hasParameterAnnotation(WxUser.class);
    }

    @Override
    public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer,
                                  NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) {
        String token = nativeWebRequest.getNativeRequest(HttpServletRequest.class).getHeader("token");
        log.info("token={}", token);

        if (Objects.isNull(token)) {
            throw new UserNotFoundException("非法请求");
        } else {
            try {
                Long userId = Long.valueOf(token.substring(0, token.length() - 33));
                Integer role = Integer.valueOf(token.substring(token.length() - 1));
                CacheLocation cacheLocation = cacheService.getCacheLocationByUserId(userId);
                if (Objects.isNull(cacheLocation)) {
                    throw new UserTokenException("token过期");
                } else {

                    User user = userMapper.selectById(userId);
                    if (Objects.isNull(user)) {
                        throw new UserNotFoundException("用户不存在");
                    }
                    log.info("user:{}",user.getEmail());
                    cacheLocation.setExpires(System.currentTimeMillis() + 24 * 60 * 60 * 1000);
                    cacheService.updateCacheLocationById(cacheLocation);
                    return user;

                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new UserTokenException("token过期,请重新登录");
            }
        }
    }
}
