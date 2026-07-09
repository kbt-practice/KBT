package com.kbt.amumal.domain.comment.controller;

import com.kbt.amumal.domain.comment.dto.CommentReqDTO;
import com.kbt.amumal.domain.comment.service.commentService;
import com.kbt.amumal.global.common.ApiResponse;
import com.kbt.amumal.global.interceptor.LoginUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "댓글", description = "댓글 생성·수정·삭제 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/comments")
public class commentController {
    private final commentService commentService;

    @Operation(summary = "댓글 생성")
    @PostMapping("/{postId}")
    public ApiResponse<?> createComment(
            @LoginUserId int userId,
            @Parameter(description = "댓글을 달 게시글 ID") @PathVariable Integer postId,
            @Valid @RequestBody CommentReqDTO.createComment request
    ) {
        int newCommentId = commentService.create(userId, postId, request);

        return ApiResponse.success("댓글 생성 성공", Map.of("commentId", newCommentId));
    }

    @Operation(summary = "댓글 수정")
    @PatchMapping("/{commentId}")
    public ApiResponse<?> updateComment(
            @LoginUserId int userId,
            @Parameter(description = "수정할 댓글 ID") @PathVariable Integer commentId,
            @Valid @RequestBody CommentReqDTO.updateComment request
    ) {
        commentService.update(userId, commentId, request);

        return ApiResponse.success("댓글 수정 성공", Map.of("commentId", commentId));
    }

    @Operation(summary = "댓글 삭제")
    @DeleteMapping("/{commentId}")
    public ApiResponse<?> deleteComment(
            @LoginUserId int userId,
            @Parameter(description = "삭제할 댓글 ID") @PathVariable Integer commentId
    ) {
        commentService.delete(userId, commentId);

        return ApiResponse.success("댓글 삭제 성공", Map.of("commentId", commentId));
    }
}
