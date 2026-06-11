package com.kbt.amumal.domain.user.controller;

import com.kbt.amumal.domain.user.dto.UserReqDTO;
import com.kbt.amumal.domain.user.dto.UserResDTO;
import com.kbt.amumal.domain.user.service.UserService;
import com.kbt.amumal.global.common.ApiResponse;
import com.kbt.amumal.global.interceptor.LoginUserId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    // 회원 가입
    @PostMapping(value = "/", consumes = "multipart/form-data")
    public ApiResponse<?> signup(@Valid @ModelAttribute UserReqDTO.Signup request) {
        String newUserId = userService.create(request);

        return ApiResponse.success("회원가입 성공", Map.of("userId", newUserId));
    }

    // 유저 조회
    @GetMapping("/")
    public ApiResponse<?> getUser(@LoginUserId int userId) {
        UserResDTO.userInfo userInfo = userService.get(userId);

        return ApiResponse.success("유저 조회 성공", userInfo);
    }

    // 회원 탈퇴
    @DeleteMapping("/withdraw")
    public ApiResponse<?> withdrawUser(@LoginUserId int userId) {
        userService.withdrawUser(userId);

        return ApiResponse.success("유저 탈퇴(비활성화) 성공", null);
    }

    // 닉네임 수정
    @PutMapping("/nickname")
    public ApiResponse<?> updateUserNickname(@LoginUserId int userId, @RequestBody @Valid UserReqDTO.UpdateNickname request) {
        userService.updateNickname(userId, request);

        return ApiResponse.success("유저 닉네임 수정 성공", null);
    }

    // 비밀번호 수정
    @PutMapping("/password")
    public ApiResponse<?> updateUserPassword(@LoginUserId int userId, @RequestBody @Valid UserReqDTO.UpdatePassword request) {
        userService.updatePassword(userId, request);

        return ApiResponse.success("유저 비밀번호 수정 성공", null);
    }

    // 프로필 이미지 수정
    @PutMapping("/profileImage")
    public ApiResponse<?> updateUserProfileImage(@LoginUserId int userId, @Valid @ModelAttribute UserReqDTO.updateProfile request) {
        userService.updateProfileImage(userId, request);

        return ApiResponse.success("유저 프로필 이미지 수정 성공", null);
    }
}
