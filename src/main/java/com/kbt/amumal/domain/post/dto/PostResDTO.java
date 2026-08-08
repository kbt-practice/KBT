package com.kbt.amumal.domain.post.dto;

import java.time.LocalDateTime;
import java.util.List;

public class PostResDTO {

    public record UserInfo(String userId, String nickname, String profileImage) {}

    public record PostListItem(
            Integer postId,
            String title,
            String postImage,
            long like,
            long comment,
            int view,
            UserInfo user,
            LocalDateTime createdAt
    ) {}

    public record Pagination(Integer nextCursor, boolean hasNext) {}

    public record PostListResponse(List<PostListItem> posts, Pagination pagination) {}

    public record CommentItem(
            Integer commentId,
            String comment,
            UserInfo user,
            LocalDateTime createdAt
    ) {}

    public record PostDetailResponse(
            Integer postId,
            String title,
            String content,
            String postImage,
            long like,
            int view,
            UserInfo user,
            LocalDateTime createdAt,
            List<CommentItem> comments
    ) {}

    public record LikeResult(String userId, Integer postId, String type) {}
}
