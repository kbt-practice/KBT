package com.kbt.amumal.domain.post.dto;

import java.time.LocalDateTime;
import java.util.List;

public class PostResDTO {

    public record userInfo(String userId, String nickname, String profileImage) {}

    public record postListItem(
            Integer postId,
            String title,
            long like,
            long comment,
            int view,
            userInfo user,
            LocalDateTime createdAt
    ) {}

    public record pagination(Integer nextCursor, boolean hasNext) {}

    public record postListResponse(List<postListItem> posts, pagination pagination) {}

    public record commentItem(
            Integer commentId,
            String comment,
            userInfo user,
            LocalDateTime createdAt
    ) {}

    public record postDetailResponse(
            Integer postId,
            String title,
            String content,
            long like,
            int view,
            userInfo user,
            LocalDateTime createdAt,
            List<commentItem> comments
    ) {}

    public record likeResult(String userId, Integer postId, String type) {}
}
