package com.kbt.amumal.global.interceptor;

import com.kbt.amumal.global.common.UserIdToken;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final UserIdToken userIdToken;

    // 컨트롤러 이전에 처리
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) return true;

        boolean requiresAuth = Arrays.stream(handlerMethod.getMethodParameters())
                .anyMatch(p -> p.hasParameterAnnotation(LoginUserId.class));

        if (!requiresAuth) return true; //

        String authorization = request.getHeader("Authorization"); // 헤더 로드
        int userId = userIdToken.getIdByToken(authorization); // JWT 파싱
        request.setAttribute("userId", userId); // ArgumentResolver에서 재사용
        return true;
    }
}
