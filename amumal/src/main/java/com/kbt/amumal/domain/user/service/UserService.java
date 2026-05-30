package com.kbt.amumal.domain.user.service;

import com.kbt.amumal.domain.user.dto.UserReqDTO;
import com.kbt.amumal.domain.user.dto.UserResDTO;
import com.kbt.amumal.domain.user.entity.User;
import com.kbt.amumal.domain.user.repository.UserRepository;
import com.kbt.amumal.global.error.CustomException;
import com.kbt.amumal.global.error.ErrorCode;
import com.kbt.amumal.global.common.ImageHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final ImageHandler fileService;

    public String create(UserReqDTO.SignupReq request) throws IOException {
        if (userRepository.findByEmail(request.getEmail()).isPresent())
            throw new CustomException(ErrorCode.CONFLICT, "중복된 이메일 입니다."); // 이메일 중복 가입 미허용

        String profileImageUrl = null; // 이미지 없으면 null로 입력
        if (request.getProfileImage() != null && !request.getProfileImage().isEmpty()) {
            profileImageUrl = fileService.save(request.getProfileImage()); // 이미지 있으면 입력된 파일로 입력
        }

        User newUser = userRepository.save(User.builder()
                .email(request.getEmail())
                .password(request.getPassword())
                .nickname(request.getNickname())
                .profileImageUrl(profileImageUrl)
                .build());

        return newUser.getUserId();
    }

    public UserResDTO.userInfoRes get(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "존재하지 않는 유저입니다."));

        return UserResDTO.userInfoRes.from(user);
    }
}