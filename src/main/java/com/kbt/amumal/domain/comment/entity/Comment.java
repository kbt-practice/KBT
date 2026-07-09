package com.kbt.amumal.domain.comment.entity;

import com.kbt.amumal.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "comments")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer commentId;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private int userId;

    @Column(nullable = false)
    private int postId;

    public void updateComment(String comment) {
        this.content = comment;
    }
}