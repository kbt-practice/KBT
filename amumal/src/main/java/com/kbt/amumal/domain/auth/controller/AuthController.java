package com.kbt.amumal.domain.auth.controller;

import com.kbt.amumal.domain.auth.dto.AuthReqDTO;
import com.kbt.amumal.domain.auth.service.AuthService;
import com.kbt.amumal.global.common.ApiResponse;
import com.kbt.amumal.global.interceptor.LoginUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "인증", description = "로그인·로그아웃 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    @Operation(summary = "로그인", description = "이메일·비밀번호로 로그인하고 JWT 액세스 토큰을 반환합니다.")
    @SecurityRequirements
    @PostMapping("/")
    public ApiResponse<?> login(@Valid @RequestBody AuthReqDTO.LoginReq request) {
        String userInfo = authService.userLogin(request);

        return ApiResponse.success("로그인 성공", Map.of("accessToken", userInfo));
    }

    @Operation(summary = "로그아웃", description = "토큰 유효성을 검증합니다. 실제 토큰 삭제는 클라이언트에서 처리합니다.")
    @PostMapping("/delete")
    public ApiResponse<?> logout(@LoginUserId int userId) {
        return ApiResponse.success("로그아웃 성공", null);
    }
}
