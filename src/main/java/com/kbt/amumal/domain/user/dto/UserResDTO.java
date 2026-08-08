package com.kbt.amumal.domain.user.dto;

import com.kbt.amumal.domain.user.entity.User;

public class UserResDTO {

    public record UserInfo(String userId, String email, String nickname, String profileImageUrl) {

        public static UserInfo from(User user) {
            return new UserInfo(user.getUserId(), user.getEmail(), user.getNickname(), user.getProfileImageUrl());
        }
    }
}
