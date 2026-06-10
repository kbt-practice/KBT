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
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final ImageHandler fileService;
    private final PasswordEncoder passwordEncoder;

    public String create(UserReqDTO.Signup request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent())
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        if (userRepository.findByNickname(request.getNickname()).isPresent())
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);

        String profileImageUrl = uploadImageIfPresent(request.getProfileImage());

        // 이미지 업로드 성공 후 DB 저장에 실패하면 트랜잭션 롤백 콜백에서 파일 정리
        registerImageRollbackOnFailure(profileImageUrl);

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

    public void updateProfileImage(int id, UserReqDTO.updateProfile request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String oldImageUrl = user.getProfileImageUrl();
        String newImageUrl = fileService.profileSave(request.getProfileImage());

        // 커밋 성공 시 기존 이미지 삭제, 롤백 시 새로 업로드한 이미지 삭제
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == TransactionSynchronization.STATUS_COMMITTED) {
                    fileService.deleteSafely(oldImageUrl);
                } else if (status == TransactionSynchronization.STATUS_ROLLED_BACK) {
                    fileService.deleteSafely(newImageUrl);
                }
            }
        });

        user.updateProfileImage(newImageUrl);
    }

    private String uploadImageIfPresent(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;
        return fileService.profileSave(file);
    }

    /**
     * 이미지가 업로드된 경우, 트랜잭션 롤백 시 해당 파일을 삭제하는 콜백을 등록한다.
     * DB 저장 실패로 인한 고아 이미지 생성을 방지하기 위해 사용된다.
     */
    private void registerImageRollbackOnFailure(String imageUrl) {
        if (imageUrl == null) return;
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == TransactionSynchronization.STATUS_ROLLED_BACK) {
                    fileService.deleteSafely(imageUrl);
                }
            }
        });
    }
}
