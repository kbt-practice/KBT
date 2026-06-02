package com.kbt.amumal.domain.auth.controller;

import com.kbt.amumal.domain.auth.dto.AuthReqDTO;
import com.kbt.amumal.domain.auth.service.AuthService;
import com.kbt.amumal.global.common.ApiResponse;
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
    private final JwtUtil jwtUtil;

    @PostMapping("/")
    public ApiResponse<?> login(@Valid @RequestBody AuthReqDTO.LoginReq request) {
        String userInfo = authService.userLogin(request);

        return ApiResponse.success("로그인 성공", Map.of("accessToken", userInfo));
    }

    @PostMapping("/delete")
    public ApiResponse<?> logout(@RequestHeader("Authorization") String authorization) {
        // 헤더가 없거나 잘못된 요청일 때
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "유저 정보를 확인해주세요.");
        }

        // "Bearer <token>" 에서 토큰 추출
        String token = authorization.replace("Bearer ", "");

        if (!jwtUtil.validateToken(token))
            throw new CustomException(ErrorCode.UNAUTHORIZED, "유저 정보를 확인해주세요.");

        String userId = jwtUtil.getUserId(token);

        // 프론트가 토큰을 삭제함
        // 현재 로컬 스토리지로 구현했다고 가정, 쿠키 관련 구현 추후 추가할 예정

        return ApiResponse.success("로그아웃 성공", Map.of("userId", userId));
    }
}