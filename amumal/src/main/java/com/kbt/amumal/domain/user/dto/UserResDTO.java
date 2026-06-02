package com.kbt.amumal.domain.user.dto;

import com.kbt.amumal.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

public class UserResDTO {
    // 유저 조회 응답 DTO
    @Getter
    @Builder
    public static class userInfo {
        private String userId;
        private String email;
        private String nickname;
        private String profileImageUrl;

        public static userInfo from(User user) {
            return userInfo.builder()
                    .userId(user.getUserId())
                    .email(user.getEmail())
                    .nickname(user.getNickname())
                    .profileImageUrl(user.getProfileImageUrl())
                    .build();
        }
    }
}
