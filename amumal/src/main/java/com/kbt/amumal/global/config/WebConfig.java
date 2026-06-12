package com.kbt.amumal.global.config;

import com.kbt.amumal.global.interceptor.AuthInterceptor;
import com.kbt.amumal.global.interceptor.LoginUserArgumentResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.profile-access-path}")
    private String profileAccessPath;

    @Value("${file.posts-access-path}")
    private String postsAccessPath;

    private final AuthInterceptor authInterceptor;
    private final LoginUserArgumentResolver loginUserArgumentResolver;

    // CORS 설정
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                        "https://www.amon.p-e.kr",
                        "https://api.amon.p-e.kr",
                        "http://localhost:3000",
                        "http://localhost:8080"
                )
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("Content-Type", "Authorization");
    }

    // 정적 리소스 경로 매핑: env의 접근 경로 요청을 실제 업로드 디렉토리로 연결
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = "file:" + uploadDir + (uploadDir.endsWith("/") ? "" : "/");
        registry.addResourceHandler(profileAccessPath + "/**").addResourceLocations(location);
        if (!postsAccessPath.equals(profileAccessPath)) {
            registry.addResourceHandler(postsAccessPath + "/**").addResourceLocations(location);
        }
    }

    // 인터셉터 - 모든 요청에 AuthInterceptor 적용 (@LoginUserId 없는 경로는 적용 안됨)
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**");
    }

    // ArgumentResolver - @LoginUserId 파라미터에 userId 자동 주입
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(loginUserArgumentResolver);
    }
}
