package com.shiguang.interceptor;

import com.shiguang.constant.JwtClaimsConstant;
import com.shiguang.context.UserContext;
import com.shiguang.properties.JwtProperties;
import com.shiguang.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 必选登录拦截器。
 *
 * 校验 Authorization: Bearer <token>，失败时直接返回 401；
 * 认证通过后把 userId 写入 UserContext 供后续层使用。
 */
@Component
@Slf4j
public class JwtTokenUserInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            writeUnauthorized(response);
            return false;
        }

        try {
            String token = auth.substring(7);
            Claims claims = JwtUtil.parseJWT(jwtProperties.getSecretKey(), token);
            UserContext.setCurrentId(claims.get(JwtClaimsConstant.USER_ID).toString());
            return true;
        } catch (Exception ex) {
            log.warn("token 校验失败：{}", ex.getMessage());
            writeUnauthorized(response);
            return false;
        }
    }

    private void writeUnauthorized(HttpServletResponse response) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"msg\":\"未登录或登录已过期\",\"data\":null}");
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        UserContext.remove();
    }
}
