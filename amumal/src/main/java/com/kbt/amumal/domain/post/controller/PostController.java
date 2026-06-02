package com.kbt.amumal.domain.post.controller;

import com.kbt.amumal.domain.post.dto.PostReqDTO;
import com.kbt.amumal.domain.post.dto.PostResDTO;
import com.kbt.amumal.domain.post.service.PostService;
import com.kbt.amumal.global.common.ApiResponse;
import com.kbt.amumal.global.common.UserIdToken;
import com.kbt.amumal.global.error.CustomException;
import com.kbt.amumal.global.error.ErrorCode;
import com.kbt.amumal.global.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {
    private final PostService postService;
    private final UserIdToken userIdToken;

    // 게시글 등록
    @PostMapping(value = "/", consumes = "multipart/form-data")
    public ApiResponse<?> newPost(@RequestHeader("Authorization") String authorization, @Valid @ModelAttribute PostReqDTO.createPost request) throws IOException {
        String userId = userIdToken.getUserIdByToken(authorization);
        int newPostId = postService.create(userId, request);

        return ApiResponse.success("게시글 생성 성공", Map.of("postId", newPostId));
    }

    // 게시글 수정
    @PatchMapping(value = "/{postId}", consumes = "multipart/form-data")
    public ApiResponse<?> newPost(
            @RequestHeader("Authorization") String authorization,
            @Valid @ModelAttribute PostReqDTO.updatePost request,
            @PathVariable Integer postId
    ) throws IOException {
        String userId = userIdToken.getUserIdByToken(authorization);
        postService.update(userId, postId, request);

        return ApiResponse.success("게시글 수정 성공", Map.of("postId", postId));
    }

    // 게시글 삭제
    @PatchMapping(value = "/{postId}")
    public ApiResponse<?> newPost(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Integer postId
    ) {
        String userId = userIdToken.getUserIdByToken(authorization);
        postService.delete(userId, postId);

        return ApiResponse.success("게시글 삭제 성공", Map.of("postId", postId));
    }

    // 게시글 조회
    @GetMapping(value = "/{postId}")
    public ApiResponse<?> newPost(@PathVariable Integer postId) {
        PostResDTO.postInfo postInfo = postService.get(postId);

        return ApiResponse.success("게시글 상세 조회 성공", postInfo);
    }
}
