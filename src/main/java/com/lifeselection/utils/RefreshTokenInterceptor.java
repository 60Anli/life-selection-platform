package com.lifeselection.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.lifeselection.dto.UserDTO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.lifeselection.utils.RedisConstants.LOGIN_USER_KEY;
import static com.lifeselection.utils.RedisConstants.LOGIN_USER_TTL;

public class RefreshTokenInterceptor implements HandlerInterceptor {

    private StringRedisTemplate stringRedisTemplate;

    public RefreshTokenInterceptor(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1.鑾峰彇璇锋眰澶翠腑鐨則oken
        String token = request.getHeader("authorization");
        if (StrUtil.isBlank(token)) {
            return true;
        }
        // 2.鍩轰簬TOKEN鑾峰彇redis涓殑鐢ㄦ埛
        String key  = LOGIN_USER_KEY + token;
        Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(key);
        // 3.鍒ゆ柇鐢ㄦ埛鏄惁瀛樺湪
        if (userMap.isEmpty()) {
            return true;
        }
        // 5.灏嗘煡璇㈠埌鐨刪ash鏁版嵁杞负UserDTO
        UserDTO userDTO = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);
        // 6.瀛樺湪锛屼繚瀛樼敤鎴蜂俊鎭埌 ThreadLocal
        UserHolder.saveUser(userDTO);
        // 7.鍒锋柊token鏈夋晥鏈?
        stringRedisTemplate.expire(key, LOGIN_USER_TTL, TimeUnit.MINUTES);
        // 8.鏀捐
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 绉婚櫎鐢ㄦ埛
        UserHolder.removeUser();
    }
}
