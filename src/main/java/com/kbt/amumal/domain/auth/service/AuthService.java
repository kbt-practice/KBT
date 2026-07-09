package com.kbt.amumal.domain.auth.service;

import com.kbt.amumal.domain.auth.dto.AuthReqDTO;
import com.kbt.amumal.domain.user.entity.User;
import com.kbt.amumal.domain.user.repository.UserRepository;
import com.kbt.amumal.global.error.CustomException;
import com.kbt.amumal.global.error.ErrorCode;
import com.kbt.amumal.global.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;

    @Value("${jwt.refresh_expiration_time}")
    private long refreshExpTimeMs;

    private static final String REFRESH_PREFIX = "RT:";

    public LoginResult userLogin(AuthReqDTO.LoginReq request) {
        User loginUser = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new CustomException(ErrorCode.LOGIN_FAILED));

        if (!passwordEncoder.matches(request.password(), loginUser.getPassword())) {
            throw new CustomException(ErrorCode.LOGIN_FAILED);
        }

        String accessToken = jwtUtil.createAccessToken(loginUser.getId(), loginUser.getEmail());
        String refreshToken = jwtUtil.createRefreshToken(loginUser.getId(), loginUser.getEmail());

        // 유저당 Refresh Token 1개만 유지 (기존 토큰 덮어쓰기 → 메모리 절약)
        redisTemplate.opsForValue().set(
                REFRESH_PREFIX + loginUser.getId(),
                refreshToken,
                Duration.ofMillis(refreshExpTimeMs)
        );

        return new LoginResult(accessToken, refreshToken);
    }

    public String refresh(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new CustomException(ErrorCode.TOKEN_MISSING);
        }

        // 만료·위변조 시 GlobalExceptionHandler가 TOKEN_EXPIRED / TOKEN_INVALID 반환
        jwtUtil.validateToken(refreshToken);

        int userId = jwtUtil.getId(refreshToken);
        String email = jwtUtil.getEmail(refreshToken);

        String savedToken = redisTemplate.opsForValue().get(REFRESH_PREFIX + userId);
        if (savedToken == null || !savedToken.equals(refreshToken)) {
            throw new CustomException(ErrorCode.TOKEN_INVALID);
        }

        return jwtUtil.createAccessToken(userId, email);
    }

    public void logout(int userId) {
        redisTemplate.delete(REFRESH_PREFIX + userId);
    }

    public record LoginResult(String accessToken, String refreshToken) {}
}
