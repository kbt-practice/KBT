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
import org.springframework.http.StreamingHttpOutputMessage;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final JwtUtil jwtUtil;

    // 회원 가입
    @PostMapping(value = "/", consumes = "multipart/form-data")
    public ApiResponse<?> signup(@Valid @ModelAttribute UserReqDTO.Signup request) throws java.io.IOException {
        String newUserId = userService.create(request);

        return ApiResponse.success("회원가입 성공", Map.of("userId", newUserId));
    }

    // 유저 조회
    @GetMapping("/")
    public ApiResponse<?> getUser(@RequestHeader("Authorization") String authorization) {
        // 헤더가 없거나 잘못된 요청일 때
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "유저 정보를 확인해주세요.");
        }

        // "Bearer <token>" 에서 토큰 추출
        String token = authorization.replace("Bearer ", "");

        if (!jwtUtil.validateToken(token))
            throw new CustomException(ErrorCode.UNAUTHORIZED, "유저 정보를 확인해주세요.");

        String userId = jwtUtil.getUserId(token);
        UserResDTO.userInfo userInfo = userService.get(userId);

        return ApiResponse.success("유저 조회 성공", userInfo);
    }

    // 닉네임 수정
    @PutMapping("/nickname")
    public ApiResponse<?> updateUser(@RequestHeader("Authorization") String authorization, @RequestBody @Valid UserReqDTO.UpdateNickname request) {
        // 헤더가 없거나 잘못된 요청일 때
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "유저 정보를 확인해주세요.");
        }

        // "Bearer <token>" 에서 토큰 추출
        String token = authorization.replace("Bearer ", "");

        if (!jwtUtil.validateToken(token))
            throw new CustomException(ErrorCode.UNAUTHORIZED, "유저 정보를 확인해주세요.");

        String userId = jwtUtil.getUserId(token);
        userService.updateNickname(userId, request.getNickname());

        return ApiResponse.success("유저 닉네임 수정 성공", Map.of("userId", userId));
    }
}
