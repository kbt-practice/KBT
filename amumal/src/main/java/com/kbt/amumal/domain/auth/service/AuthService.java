package com.kbt.amumal.domain.auth.service;

import com.kbt.amumal.domain.auth.dto.AuthReqDTO;
import com.kbt.amumal.domain.user.dto.UserReqDTO;
import com.kbt.amumal.domain.user.entity.User;
import com.kbt.amumal.domain.user.repository.UserRepository;
import com.kbt.amumal.global.error.CustomException;
import com.kbt.amumal.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;

    public String userLogin(AuthReqDTO.LoginReq request) {
        User loginUser = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED, "아이디 또는 비밀번호를 확인해주세요."));

        if (!loginUser.getPassword().equals(request.getPassword())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED, "아이디 또는 비밀번호를 확인해주세요.");
        }

        return loginUser.getUserId();
    }
}
