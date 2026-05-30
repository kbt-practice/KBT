package com.kbt.amumal.domain.user.service;

import com.kbt.amumal.domain.user.dto.UserReqDTO;
import com.kbt.amumal.domain.user.dto.UserResDTO;
import com.kbt.amumal.domain.user.entity.User;
import com.kbt.amumal.domain.user.repository.UserRepository;
import com.kbt.amumal.global.error.CustomException;
import com.kbt.amumal.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public String create(UserReqDTO.SignupReq request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new CustomException(ErrorCode.CONFLICT, "이미 존재하는 이메일입니다.");
        }

        User newUser = userRepository.save(
                new User(null, request.getEmail(), request.getPassword(), request.getNickname(), request.getProfileImageUrl())
        );

        return newUser.getUserId();
    }

    public UserResDTO.userInfoRes get(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "존재하지 않는 유저입니다."));

        return UserResDTO.userInfoRes.from(user);
    }
}