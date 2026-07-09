package com.kbt.amumal.global.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class LoginUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(LoginUserId.class);
    }

    // 인터셉터에서 request.setAttribute("userId", userId)로 저장해둔 값을 꺼내서 반환
    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        Class<?> type = parameter.getParameterType();
        if (type != int.class && type != Integer.class) {
            throw new IllegalStateException("@LoginUserId는 int 또는 Integer 타입에만 사용할 수 있습니다.");
        }

        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        return request.getAttribute("userId"); // @LoginUserId 파라미터에 자동으로 주입
    }
}
