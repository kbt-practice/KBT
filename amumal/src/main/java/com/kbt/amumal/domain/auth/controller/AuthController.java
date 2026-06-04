package com.kbt.amumal.domain.auth.controller;

import com.kbt.amumal.domain.auth.dto.AuthReqDTO;
import com.kbt.amumal.domain.auth.service.AuthService;
import com.kbt.amumal.global.common.ApiResponse;
import com.kbt.amumal.global.common.UserIdToken;
import com.kbt.amumal.global.error.CustomException;
import com.kbt.amumal.global.error.ErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.kbt.amumal.global.util.JwtUtil;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private final UserIdToken userIdToken;

    @PostMapping("/")
    public ApiResponse<?> login(@Valid @RequestBody AuthReqDTO.LoginReq request) {
        String userInfo = authService.userLogin(request);

        return ApiResponse.success("로그인 성공", Map.of("accessToken", userInfo));
    }

    @PostMapping("/delete")
    public ApiResponse<?> logout(@RequestHeader("Authorization") String authorization) {
        String userId = userIdToken.getUserIdByToken(authorization);

        // 프론트가 토큰을 삭제함
        // 현재 로컬 스토리지로 구현했다고 가정, 쿠키 관련 구현 추후 추가할 예정

        return ApiResponse.success("로그아웃 성공", Map.of("userId", userId));
    }
}