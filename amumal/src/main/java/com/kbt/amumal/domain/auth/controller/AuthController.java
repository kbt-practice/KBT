package com.kbt.amumal.domain.auth.controller;

import com.kbt.amumal.domain.auth.dto.AuthReqDTO;
import com.kbt.amumal.domain.auth.service.AuthService;
import com.kbt.amumal.global.common.ApiResponse;
import com.kbt.amumal.global.interceptor.LoginUserId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/")
    public ApiResponse<?> login(@Valid @RequestBody AuthReqDTO.LoginReq request) {
        String userInfo = authService.userLogin(request);

        return ApiResponse.success("로그인 성공", Map.of("accessToken", userInfo));
    }

    @PostMapping("/delete")
    public ApiResponse<?> logout(@LoginUserId int userId) {
        // 토큰 유효성은 인터셉터에서 검증, 실제 삭제는 프론트에서 처리
        return ApiResponse.success("로그아웃 성공", null);
    }
}
