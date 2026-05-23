package com.lifeselection.utils;

import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1.鍒ゆ柇鏄惁闇€瑕佹嫤鎴紙ThreadLocal涓槸鍚︽湁鐢ㄦ埛锛?
        if (UserHolder.getUser() == null) {
            // 娌℃湁锛岄渶瑕佹嫤鎴紝璁剧疆鐘舵€佺爜
            response.setStatus(401);
            // 鎷︽埅
            return false;
        }
        // 鏈夌敤鎴凤紝鍒欐斁琛?
        return true;
    }
}
