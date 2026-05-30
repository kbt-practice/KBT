package com.kbt.amumal.domain.user.controller;

import com.kbt.amumal.domain.user.dto.UserReqDTO;
import com.kbt.amumal.domain.user.dto.UserResDTO;
import com.kbt.amumal.domain.user.service.UserService;
import com.kbt.amumal.global.common.ApiResponse;
import com.kbt.amumal.global.error.CustomException;
import com.kbt.amumal.global.error.ErrorCode;
import com.kbt.amumal.global.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @PostMapping(value = "/", consumes = "multipart/form-data")
    public ApiResponse<?> signup(@Valid @ModelAttribute UserReqDTO.SignupReq request) throws java.io.IOException {
        String newUserId = userService.create(request);

        return ApiResponse.success("회원가입 성공", Map.of("userId", newUserId));
    }

    @GetMapping("/")
    public ApiResponse<UserResDTO.userInfoRes> getUser(@RequestHeader("Authorization") String authorization) {
        // "Bearer <token>" 에서 토큰 추출
        String token = authorization.replace("Bearer ", "");

        if (!jwtUtil.validateToken(token))
            throw new CustomException(ErrorCode.UNAUTHORIZED, "유효하지 않은 토큰입니다.");

        String userId = jwtUtil.getUserId(token);
        UserResDTO.userInfoRes userInfo = userService.get(userId);

        return ApiResponse.success("유저 조회 성공", userInfo);
    }
}
