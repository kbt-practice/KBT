package com.kbt.amumal.domain.user.controller;

import com.kbt.amumal.domain.user.dto.UserReqDTO;
import com.kbt.amumal.domain.user.dto.UserResDTO;
import com.kbt.amumal.domain.user.service.UserService;
import com.kbt.amumal.global.common.ApiResponse;
import com.kbt.amumal.global.interceptor.LoginUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "유저", description = "회원가입·정보 조회·수정·탈퇴 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @Operation(summary = "회원가입", description = "이메일·비밀번호·닉네임으로 가입합니다. 프로필 이미지는 선택입니다.")
    @SecurityRequirements
    @PostMapping(value = "/", consumes = "multipart/form-data")
    public ApiResponse<?> signup(@Valid @ModelAttribute UserReqDTO.Signup request) {
        String newUserId = userService.create(request);

        return ApiResponse.success("회원가입 성공", Map.of("userId", newUserId));
    }

    @Operation(summary = "내 정보 조회", description = "JWT 토큰으로 인증된 유저의 정보를 반환합니다.")
    @GetMapping("/")
    public ApiResponse<?> getUser(@LoginUserId int userId) {
        UserResDTO.userInfo userInfo = userService.get(userId);

        return ApiResponse.success("유저 조회 성공", userInfo);
    }

    @Operation(summary = "회원 탈퇴", description = "계정을 소프트 딜리트합니다. 7일 후 하드 딜리트됩니다.")
    @DeleteMapping("/withdraw")
    public ApiResponse<?> withdrawUser(@LoginUserId int userId) {
        userService.withdrawUser(userId);

        return ApiResponse.success("유저 탈퇴(비활성화) 성공", null);
    }

    @Operation(summary = "닉네임 수정")
    @PutMapping("/nickname")
    public ApiResponse<?> updateUserNickname(@LoginUserId int userId, @RequestBody @Valid UserReqDTO.UpdateNickname request) {
        userService.updateNickname(userId, request);

        return ApiResponse.success("유저 닉네임 수정 성공", null);
    }

    @Operation(summary = "비밀번호 수정")
    @PutMapping("/password")
    public ApiResponse<?> updateUserPassword(@LoginUserId int userId, @RequestBody @Valid UserReqDTO.UpdatePassword request) {
        userService.updatePassword(userId, request);

        return ApiResponse.success("유저 비밀번호 수정 성공", null);
    }

    @Operation(summary = "프로필 이미지 수정")
    @PutMapping(value = "/profileImage", consumes = "multipart/form-data")
    public ApiResponse<?> updateUserProfileImage(@LoginUserId int userId, @Valid @ModelAttribute UserReqDTO.updateProfile request) {
        userService.updateProfileImage(userId, request);

        return ApiResponse.success("유저 프로필 이미지 수정 성공", null);
    }
}
