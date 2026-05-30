package com.kbt.amumal.domain.auth.controller;

import com.kbt.amumal.domain.auth.dto.AuthReqDTO;
import com.kbt.amumal.domain.auth.service.AuthService;
import com.kbt.amumal.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/")
    public ApiResponse<?> login(@Valid @RequestBody AuthReqDTO.LoginReq request) {
        String userInfo = authService.userLogin(request);

        return ApiResponse.success("로그인 성공", Map.of("userId", userInfo));
    }
}
