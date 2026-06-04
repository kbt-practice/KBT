package com.kbt.amumal.domain.user.entity;

import com.kbt.amumal.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(length = 36, unique = true, nullable = false)
    private String userId;

    @Column(nullable = false, unique = true, length = 50)
    private String email;

    @Column(nullable = false, length = 200)
    private String password;

    @Column(nullable = false, unique = true, length = 10)
    private String nickname;

    @Column(length = 500)
    private String profileImageUrl;

    @PrePersist
    private void generateUserId() {
        if (userId == null) {
            userId = UUID.randomUUID().toString();
        }
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updatePassword(String password) {
        this.password = password;
    }

    public void updateProfileImage(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}
