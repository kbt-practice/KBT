package com.kbt.amumal.domain.user.service;

import com.kbt.amumal.domain.user.dto.UserReqDTO;
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

    public int create(UserReqDTO.SignupReq request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new CustomException(ErrorCode.CONFLICT, "이미 존재하는 이메일입니다.");
        }

        User newUser = userRepository.save(
                new User(0, request.getEmail(), request.getPassword(), request.getNickname(), request.getProfileImageUrl())
        );

        return newUser.getUserId();
    }
}