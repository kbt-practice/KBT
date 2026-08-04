package com.kbt.amumal.domain.post.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "likes", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "post_id"}))
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Like {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long likeId;

    // 좋아요를 누른 유저가 탈퇴(하드 딜리트)하면 null로 익명화되고 좋아요 자체는 그대로 남는다
    private Integer userId;

    @Column(nullable = false)
    private Integer postId;
}