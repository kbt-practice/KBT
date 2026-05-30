package com.kbt.amumal.domain.user.controller;

import com.kbt.amumal.domain.user.dto.UserReqDTO;
import com.kbt.amumal.domain.user.dto.UserResDTO;
import com.kbt.amumal.domain.user.service.UserService;
import com.kbt.amumal.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @PostMapping("/")
    public ApiResponse<?> signup(@RequestBody UserReqDTO.SignupReq request) {
        String newUserId = userService.create(request);

        return ApiResponse.success("회원가입 성공", Map.of("userId", newUserId));
    }

    @GetMapping("/")
    public ApiResponse<UserResDTO.userInfoRes> getUser(@RequestHeader("userId") UserReqDTO.userInfoReq request) {
        UserResDTO.userInfoRes userInfo = userService.get(request.getUserId());

        return ApiResponse.success("유저 조회 성공", userInfo);
    }
}