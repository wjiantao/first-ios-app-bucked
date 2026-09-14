package com.shiguang.config;

import com.shiguang.interceptor.JwtTokenUserInterceptor;
import com.shiguang.properties.UploadProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Web MVC 配置：注册登录鉴权拦截器、跨域规则与上传文件静态资源映射。
 *
 * 需要登录的接口（/api/users/**、/api/works 的发布与详情等）统一走 JWT 校验；
 * 首页游客可读的只读接口放行，未登录也能拉取分类与作品流：
 * - GET /api/categories：首页分类 Tabs / 发布页分类选择器共用；
 * - POST /api/works/page：首页瀑布流分页查询；该接口允许游客访问，
 *   但会尝试读取可选 JWT，供“关注”频道按当前用户过滤。
 * - POST /api/map/works：Cesium 当前可视范围作品点位查询。
 * /api/auth/** 为公开登录接口，不注册拦截器。
 */
@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Autowired
    private JwtTokenUserInterceptor jwtTokenUserInterceptor;

    @Autowired
    private UploadProperties uploadProperties;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtTokenUserInterceptor)
                .addPathPatterns("/api/users/**", "/api/categories/**", "/api/works/**", "/api/creator/**",
                        "/api/map/checkins",
                        "/api/notifications/**", "/api/push/**")
                .excludePathPatterns(
                        // 首页游客可读接口：放行后未登录即可浏览首页；
                        // JwtTokenUserInterceptor 仍会为该接口读取有效 JWT，
                        // 因此登录用户的“关注”频道可以获得 UserContext 身份。
                        "/api/categories");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 把上传目录暴露为 /uploads/**，与上传接口返回的路径对应
        Path uploadDir = Paths.get(uploadProperties.getDir()).toAbsolutePath().normalize();
        String location = uploadDir.toUri().toString();
        if (!location.endsWith("/")) {
            location += "/";
        }
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 开发阶段放开跨域；RN 原生请求不受浏览器同源策略限制，此项主要方便 Web 调试
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
