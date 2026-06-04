package com.kbt.amumal.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// Spring MVC 설정 커스터마이징 (CORS, 인터셉터, 정적 리소스 등)
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/profiles/**") // /profiles/ 로 이미지 조회할 수 있도록 함
                .addResourceLocations("file:" + System.getProperty("user.dir") + "/" + uploadDir + "/"); // 해당 경로에서 파일을 찾아 반환
    }
}
