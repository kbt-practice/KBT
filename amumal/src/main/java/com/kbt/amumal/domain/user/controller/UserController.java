package com.kbt.amumal.domain.user.controller;

import com.kbt.amumal.domain.user.dto.UserReqDTO;
import com.kbt.amumal.domain.user.dto.UserResDTO;
import com.kbt.amumal.domain.user.service.UserService;
import com.kbt.amumal.global.common.ApiResponse;
import com.kbt.amumal.global.common.UserIdToken;
import com.kbt.amumal.global.error.CustomException;
import com.kbt.amumal.global.error.ErrorCode;
import com.kbt.amumal.global.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final UserIdToken userIdToken;

    // 회원 가입
    @PostMapping(value = "/", consumes = "multipart/form-data")
    public ApiResponse<?> signup(@Valid @ModelAttribute UserReqDTO.Signup request) throws IOException {
        String newUserId = userService.create(request);

        return ApiResponse.success("회원가입 성공", Map.of("userId", newUserId));
    }

    // 유저 조회
    @GetMapping("/")
    public ApiResponse<?> getUser(@RequestHeader("Authorization") String authorization) {
        String userId = userIdToken.getUserIdByToken(authorization);
        UserResDTO.userInfo userInfo = userService.get(userId);

        return ApiResponse.success("유저 조회 성공", userInfo);
    }

    // 회원 탈퇴
    @PatchMapping("/withdraw")
    public ApiResponse<?> withdrawUser(@RequestHeader("Authorization") String authorization) {
        String userId = userIdToken.getUserIdByToken(authorization);
        userService.withdrawUser(userId);

        return ApiResponse.success("유저 탈퇴(비활성화) 성공", Map.of("userId", userId));
    }

    // 닉네임 수정
    @PutMapping("/nickname")
    public ApiResponse<?> updateUserNickname(@RequestHeader("Authorization") String authorization, @RequestBody @Valid UserReqDTO.UpdateNickname request) {
        String userId = userIdToken.getUserIdByToken(authorization);
        userService.updateNickname(userId, request);

        return ApiResponse.success("유저 닉네임 수정 성공", Map.of("userId", userId));
    }

    // 비밀번호 수정
    @PutMapping("/password")
    public ApiResponse<?> updateUserPassword(@RequestHeader("Authorization") String authorization, @RequestBody @Valid UserReqDTO.UpdatePassword request) {
        String userId = userIdToken.getUserIdByToken(authorization);
        userService.updatePassword(userId, request);

        return ApiResponse.success("유저 비밀번호 수정 성공", Map.of("userId", userId));
    }

    // 프로필 이미지 수정
    @PutMapping("/profileImage")
    public ApiResponse<?> updateUserProfileImage(@RequestHeader("Authorization") String authorization, @Valid @ModelAttribute UserReqDTO.updateProfile request) throws IOException {
        String userId = userIdToken.getUserIdByToken(authorization);
        userService.updateProfileImage(userId, request);

        return ApiResponse.success("유저 프로필 이미지 수정 성공", Map.of("userId", userId));
    }
}
