package com.kbt.amumal.global.config;

import com.kbt.amumal.global.interceptor.LoginUserId;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${server.url:http://localhost:8080}")
    private String serverUrl;

    static {
        // @LoginUserId 파라미터는 인터셉터가 처리하므로 Swagger 문서에서 숨김
        SpringDocUtils.getConfig().addAnnotationsToIgnore(LoginUserId.class);
    }

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("아무말 API")
                        .description("아무말 커뮤니티 서비스 REST API 문서")
                        .version("1.0.0"))
                .servers(List.of(
                        new Server().url(serverUrl).description("운영 서버"),
                        new Server().url("http://localhost:8080").description("로컬 테스트")
                ))
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("BearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("로그인 후 발급받은 JWT 토큰을 입력하세요.")));
    }
}
