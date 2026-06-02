package com.kbt.amumal.global.common;

import com.kbt.amumal.global.error.CustomException;
import com.kbt.amumal.global.error.ErrorCode;
import com.kbt.amumal.global.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserIdToken {
    private final JwtUtil jwtUtil;

    public String getUserIdByToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer "))
            throw new CustomException(ErrorCode.BAD_REQUEST, "유저 정보를 확인해주세요.");

        String token = authorization.replace("Bearer ", "");

        if (!jwtUtil.validateToken(token))
            throw new CustomException(ErrorCode.UNAUTHORIZED, "유저 정보를 확인해주세요.");

        return jwtUtil.getUserId(token);
    }
}
