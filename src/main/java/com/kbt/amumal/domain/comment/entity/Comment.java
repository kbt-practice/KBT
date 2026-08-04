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

    // 작성자가 탈퇴(하드 딜리트)하면 null로 익명화되고 댓글 내용은 그대로 남는다
    private Integer userId;

    @Column(nullable = false)
    private int postId;

    public void updateComment(String comment) {
        this.content = comment;
    }
}