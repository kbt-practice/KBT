package com.kbt.amumal.domain.user.dto;

import com.kbt.amumal.domain.user.entity.User;

public class UserResDTO {

    public record userInfo(String userId, String email, String nickname, String profileImageUrl) {

        public static userInfo from(User user) {
            return new userInfo(user.getUserId(), user.getEmail(), user.getNickname(), user.getProfileImageUrl());
        }
    }
}
