package com.kbt.amumal.domain.comment.controller;

import com.kbt.amumal.domain.comment.dto.CommentReqDTO;
import com.kbt.amumal.domain.comment.service.commentService;
import com.kbt.amumal.global.common.ApiResponse;
import com.kbt.amumal.global.interceptor.LoginUserId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comments")
public class commentController {
    private final commentService commentService;

    // 댓글 생성
    @PostMapping("/{postId}")
    public ApiResponse<?> createComment(
            @LoginUserId int userId,
            @PathVariable Integer postId,
            @Valid @RequestBody CommentReqDTO.createComment request
    ) {
        int newCommentId = commentService.create(userId, postId, request);

        return ApiResponse.success("댓글 생성 성공", Map.of("commentId", newCommentId));
    }

    // 댓글 수정
    @PatchMapping("/{commentId}")
    public ApiResponse<?> updateComment(
            @LoginUserId int userId,
            @PathVariable Integer commentId,
            @Valid @RequestBody CommentReqDTO.updateComment request
    ) {
        commentService.update(userId, commentId, request);

        return ApiResponse.success("댓글 수정 성공", Map.of("commentId", commentId));
    }

    // 댓글 삭제
    @DeleteMapping("/{commentId}")
    public ApiResponse<?> deleteComment(
            @LoginUserId int userId,
            @PathVariable Integer commentId
    ) {
        commentService.delete(userId, commentId);

        return ApiResponse.success("댓글 삭제 성공", Map.of("commentId", commentId));
    }
}
