package com.kbt.amumal.domain.user.service;

import com.kbt.amumal.domain.user.dto.UserReqDTO;
import com.kbt.amumal.domain.user.dto.UserResDTO;
import com.kbt.amumal.domain.user.entity.User;
import com.kbt.amumal.domain.user.repository.UserRepository;
import com.kbt.amumal.global.error.CustomException;
import com.kbt.amumal.global.error.ErrorCode;
import com.kbt.amumal.global.common.ImageHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final ImageHandler fileService;
    private final PasswordEncoder passwordEncoder;

    // 유저 추가
    public String create(UserReqDTO.Signup request) throws IOException {
        if (userRepository.findByEmail(request.getEmail()).isPresent())
            throw new CustomException(ErrorCode.CONFLICT, "중복된 이메일 입니다.");

        String profileImageUrl = null;
        if (request.getProfileImage() != null && !request.getProfileImage().isEmpty()) {
            profileImageUrl = fileService.profileSave(request.getProfileImage());
        }

        User newUser = userRepository.save(User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .profileImageUrl(profileImageUrl)
                .build());

        return newUser.getUserId();
    }

    // 유저 정보 조회
    public UserResDTO.userInfo get(int id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "유저 정보를 확인해주세요."));

        return UserResDTO.userInfo.from(user);
    }

    // 유저 비활성화
    public void withdrawUser(int id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "유저 정보를 확인해주세요."));

        if (user.getDeletedAt() != null)
            throw new CustomException(ErrorCode.CONFLICT, "이미 탈퇴한 유저입니다.");

        user.softDelete();
    }

    // 닉네임 수정
    public void updateNickname(int id, UserReqDTO.UpdateNickname request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "유저 정보를 확인해주세요."));

        if (userRepository.findByNickname(request.getNickname()).isPresent())
            throw new CustomException(ErrorCode.CONFLICT, "중복된 닉네임 입니다.");

        user.updateNickname(request.getNickname());
    }

    // 비밀번호 수정
    public void updatePassword(int id, UserReqDTO.UpdatePassword request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "유저 정보를 확인해주세요."));

        user.updatePassword(passwordEncoder.encode(request.getPassword()));
    }

    // 프로필 이미지 수정
    public void updateProfileImage(int id, UserReqDTO.updateProfile request) throws IOException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "유저 정보를 확인해주세요."));

        String profileImageUrl = fileService.profileSave(request.getProfileImage());

        user.updateProfileImage(profileImageUrl);
    }
}