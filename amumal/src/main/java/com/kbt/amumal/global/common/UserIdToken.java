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

    public int getIdByToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer "))
            throw new CustomException(ErrorCode.BAD_REQUEST, "잘못된 토큰입니다.");

        String token = authorization.replace("Bearer ", "");

        if (!jwtUtil.validateToken(token))
            throw new CustomException(ErrorCode.BAD_REQUEST, "잘못된 토큰입니다.");

        return jwtUtil.getId(token);
    }
}