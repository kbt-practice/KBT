package com.kbt.amumal.domain.post.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

public class PostResDTO {
    // 유저 정보
    @Getter
    @Builder
    public static class userInfo {
        private String userId;
        private String nickname;
        private String profileImage;
    }

    // 게시글 리스트 중 하나
    @Getter
    @Builder
    public static class postListItem {
        private Integer postId;
        private String title;
        private long like;
        private long comment;
        private int view;
        private userInfo user;
        private LocalDateTime createdAt;
    }

    // 페이지네이션
    @Getter
    @Builder
    public static class pagination {
        private Integer nextCursor;
        private boolean hasNext;
    }

    // 게시글 목록 조회 응답 DTO - 최종 (유저 정보 + 게시글 + 페이지네이션)
    @Getter
    @Builder
    public static class postListResponse {
        private List<postListItem> posts;
        private pagination pagination;
    }

    // 댓글 조회 응답 DTO
    @Getter
    @Builder
    public static class commentItem {
        private Integer commentId;
        private String comment;
        private userInfo user;
        private LocalDateTime createdAt;
    }

    // 게시글 상세 조회 응답 DTO
    @Getter
    @Builder
    public static class postDetailResponse {
        private Integer postId;
        private String title;
        private String content;
        private long like;
        private int view;
        private userInfo user;
        private LocalDateTime createdAt;
        private List<commentItem> comments;
    }

    // 좋아요 결과 응답 DTO
    @Getter
    @Builder
    public static class likeResult {
        private String userId;
        private Integer postId;
        private String type; // CREATE, DELETE
    }
}