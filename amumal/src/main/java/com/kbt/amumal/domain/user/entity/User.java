package com.kbt.amumal.domain.user.entity;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
public class User {
    private String userId;
    private String email;
    private String password;
    private String nickname;
    private String profileImageUrl;
}