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
        // 作品分页同时服务推荐流和关注流：推荐允许游客访问，关注流再由
        // WorkServiceImpl 根据 followingOnly 强制校验登录。这里不能直接放过
        // 整个接口，否则已登录用户的 token 永远不会写入 UserContext，关注流
        // 就无法知道应该按哪个用户的关系过滤。
        if (isOptionalWorkPage(request)) {
            if (auth == null || !auth.startsWith("Bearer ")) {
                return true;
            }
            try {
                String token = auth.substring(7);
                Claims claims = JwtUtil.parseJWT(jwtProperties.getSecretKey(), token);
                Object userId = claims.get(JwtClaimsConstant.USER_ID);
                if (userId != null) {
                    UserContext.setCurrentId(userId.toString());
                }
            } catch (Exception ex) {
                // 推荐流仍是公开接口，失效 token 不应阻塞游客浏览；
                // 若请求的是关注流，Service 层会因没有有效 viewerId 返回 401。
                log.debug("可选 token 校验失败，按游客请求处理：{}", ex.getMessage());
            }
            return true;
        }

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

    /** 判断当前请求是否为允许游客访问、但支持读取登录身份的作品分页接口。 */
    private boolean isOptionalWorkPage(HttpServletRequest request) {
        return "/api/works/page".equals(request.getRequestURI())
                && "POST".equalsIgnoreCase(request.getMethod());
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
