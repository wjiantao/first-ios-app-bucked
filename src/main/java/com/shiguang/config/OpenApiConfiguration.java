package com.shiguang.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger / OpenAPI 文档配置。
 *
 * 页面地址：http://localhost:8082/swagger-ui/index.html
 * 原始 JSON：http://localhost:8082/v3/api-docs
 * JWT 接口可在页面右上角 Authorize 中输入 Bearer Token 后直接调试。
 */
@Configuration
public class OpenApiConfiguration {

    public static final String BEARER_AUTH = "BearerAuth";

    @Bean
    public OpenAPI shiguangOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("拾光服务端 API")
                        .description("「拾光」个人内容 App 服务端接口文档。\n\n"
                                + "统一返回结构：{code, msg, data}，code=1 表示成功，其余为失败码（与 HTTP 状态码保持一致）。\n"
                                + "分类列表与作品分页列表对游客开放；用户资料、发布/查看详情等接口需登录，\n"
                                + "请在右上角 Authorize 中填写 Bearer <token> 后再调试。\n\n"
                                + "页面地址：http://localhost:8080/swagger-ui/index.html\n"
                                + "原始 JSON：http://localhost:8080/v3/api-docs\n"
                                + "原始 YAML：http://localhost:8080/v3/api-docs.yaml")
                        .version("0.1.0"))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("登录后获取的 token，格式：Bearer <token>")));
    }
}
