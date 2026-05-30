package com.kbt.amumal.domain.user.controller;

import com.kbt.amumal.domain.user.dto.UserReqDTO;
import com.kbt.amumal.domain.user.dto.UserResDTO;
import com.kbt.amumal.domain.user.service.UserService;
import com.kbt.amumal.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @PostMapping(value = "/", consumes = "multipart/form-data")
    public ApiResponse<?> signup(@Valid @ModelAttribute UserReqDTO.SignupReq request) throws java.io.IOException {
        String newUserId = userService.create(request);

        return ApiResponse.success("회원가입 성공", Map.of("userId", newUserId));
    }

    @GetMapping("/")
    public ApiResponse<UserResDTO.userInfoRes> getUser(@RequestHeader("userId") String userId) {
        UserResDTO.userInfoRes userInfo = userService.get(userId);

        return ApiResponse.success("유저 조회 성공", userInfo);
    }
}