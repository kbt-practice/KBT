package com.kbt.amumal.domain.post.entity;

import com.kbt.amumal.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.Builder;

@Entity
@Table(name = "posts")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Post extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer postId;

    @Column(nullable = false, length = 50)
    private String title;

    @Column(nullable = false)
    private String content;

    @Column(length = 500)
    private String postImageUrl;

    @Column(length = 36)
    private String userId;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private int viewCount = 0;

    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void updatePostImage(String imageUrl) {
        this.postImageUrl = imageUrl;
    }

    public void incrementViewCount() {
        this.viewCount++;
    }
}