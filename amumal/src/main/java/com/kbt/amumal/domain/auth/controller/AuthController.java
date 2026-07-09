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
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "인증", description = "로그인·로그아웃·토큰 재발급 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "로그인", description = "이메일·비밀번호로 로그인합니다. Access Token은 body, Refresh Token은 HttpOnly 쿠키로 반환됩니다.")
    @SecurityRequirements
    @PostMapping("/")
    public ResponseEntity<ApiResponse<?>> login(@Valid @RequestBody AuthReqDTO.LoginReq request) {
        AuthService.LoginResult result = authService.userLogin(request);

        ResponseCookie refreshCookie = buildRefreshCookie(result.refreshToken(), 30L * 24 * 60 * 60);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(ApiResponse.success("로그인 성공", Map.of("accessToken", result.accessToken())));
    }

    @Operation(summary = "Access Token 재발급", description = "HttpOnly 쿠키의 Refresh Token으로 새 Access Token을 발급합니다.")
    @SecurityRequirements
    @PostMapping("/refresh")
    public ApiResponse<?> refresh(
            @CookieValue(name = "refreshToken", required = false) String refreshToken) {
        String newAccessToken = authService.refresh(refreshToken);
        return ApiResponse.success("토큰 재발급 성공", Map.of("accessToken", newAccessToken));
    }

    @Operation(summary = "로그아웃", description = "Refresh Token을 Redis에서 삭제하고 쿠키를 만료시킵니다.")
    @PostMapping("/delete")
    public ResponseEntity<ApiResponse<?>> logout(@LoginUserId int userId) {
        authService.logout(userId);

        // maxAge=0 으로 쿠키 즉시 만료
        ResponseCookie expiredCookie = buildRefreshCookie("", 0);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, expiredCookie.toString())
                .body(ApiResponse.success("로그아웃 성공", null));
    }

    /**
     * Refresh Token 쿠키 빌더
     * - HttpOnly: JS 접근 차단
     * - Secure: HTTPS 전용
     * - SameSite=None: 크로스 도메인 허용
     * - Domain=.amon.p-e.kr: 서브도메인 간 쿠키 공유
     * - Path=/auth: 토큰 갱신·로그아웃 경로에만 전송
     */
    private ResponseCookie buildRefreshCookie(String value, long maxAgeSeconds) {
        return ResponseCookie.from("refreshToken", value)
                .httpOnly(true)
                .sameSite("None")
                .secure(true)
                .domain(".amon.p-e.kr")
                .path("/")
                .maxAge(maxAgeSeconds)
                .build();
    }
}