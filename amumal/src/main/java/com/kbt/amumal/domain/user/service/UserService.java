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
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final ImageHandler fileService;

    public String create(UserReqDTO.Signup request) throws IOException {
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

    // 유저 정보 조회
    public UserResDTO.userInfo get(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "유저 정보를 확인해주세요.")); // 유저 존재 안할 시

        return UserResDTO.userInfo.from(user);
    }

    // 닉네임 수정
    public void updateNickname(String userId, String nickname) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "유저 정보를 확인해주세요.")); // 유저 존재 안할 시

        if (userRepository.findByNickname(nickname).isPresent())
            throw new CustomException(ErrorCode.CONFLICT, "중복된 닉네임 입니다."); // 닉네임 중복 설정 미허용

        user.updateNickname(nickname);
    }

    // 비밀번호 수정
    public void updatePassword(String userId, String password) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "유저 정보를 확인해주세요.")); // 유저 존재 안할 시

        user.updatePassword(password);
    }
}