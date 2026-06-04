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

    public String create(UserReqDTO.Signup request) throws IOException {
        if (userRepository.findByEmail(request.getEmail()).isPresent())
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);

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

    public UserResDTO.userInfo get(int id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return UserResDTO.userInfo.from(user);
    }

    public void withdrawUser(int id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (user.getDeletedAt() != null)
            throw new CustomException(ErrorCode.USER_ALREADY_WITHDRAWN);

        user.softDelete();
    }

    public void updateNickname(int id, UserReqDTO.UpdateNickname request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (userRepository.findByNickname(request.getNickname()).isPresent())
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);

        user.updateNickname(request.getNickname());
    }

    public void updatePassword(int id, UserReqDTO.UpdatePassword request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        user.updatePassword(passwordEncoder.encode(request.getPassword()));
    }

    public void updateProfileImage(int id, UserReqDTO.updateProfile request) throws IOException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String profileImageUrl = fileService.profileSave(request.getProfileImage());

        user.updateProfileImage(profileImageUrl);
    }
}
