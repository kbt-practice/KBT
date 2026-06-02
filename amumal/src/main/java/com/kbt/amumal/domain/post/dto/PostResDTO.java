package com.kbt.amumal.domain.post.dto;

import com.kbt.amumal.domain.post.entity.Post;
import com.kbt.amumal.domain.user.dto.UserResDTO;
import lombok.Builder;
import lombok.Getter;

public class PostResDTO {
    @Getter
    @Builder
    public static class postInfo {
        private String userId;
        private String title;
        private String content;
        private String postImageUrl;

        public static postInfo from(Post post) {
            return postInfo.builder()
                    .userId(post.getUserId())
                    .title(post.getTitle())
                    .content(post.getContent())
                    .postImageUrl(post.getPostImageUrl())
                    .build();
        }
    }
}