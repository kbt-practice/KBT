package com.kbt.amumal.domain.auth.service;

import com.kbt.amumal.domain.auth.dto.AuthReqDTO;
import com.kbt.amumal.domain.user.entity.User;
import com.kbt.amumal.domain.user.repository.UserRepository;
import com.kbt.amumal.global.error.CustomException;
import com.kbt.amumal.global.error.ErrorCode;
import com.kbt.amumal.global.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public String userLogin(AuthReqDTO.LoginReq request) {
        User loginUser = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED, "아이디 또는 비밀번호를 확인해주세요."));

        if (!passwordEncoder.matches(request.getPassword(), loginUser.getPassword())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED, "아이디 또는 비밀번호를 확인해주세요.");
        }

        // 로그인 성공 시 JWT 발급
        return jwtUtil.createAccessToken(loginUser.getId(), loginUser.getEmail());
    }
}
