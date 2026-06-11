package com.kbt.amumal.domain.post.controller;

import com.kbt.amumal.domain.post.dto.PostReqDTO;
import com.kbt.amumal.domain.post.dto.PostResDTO;
import com.kbt.amumal.domain.post.service.PostService;
import com.kbt.amumal.global.common.ApiResponse;
import com.kbt.amumal.global.interceptor.LoginUserId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {
    private final PostService postService;

    // 게시글 등록
    @PostMapping(value = "/", consumes = "multipart/form-data")
    public ApiResponse<?> createPost(@LoginUserId int userId, @Valid @ModelAttribute PostReqDTO.createPost request) {
        int newPostId = postService.create(userId, request);

        return ApiResponse.success("게시글 생성 성공", Map.of("postId", newPostId));
    }

    // 게시글 수정
    @PatchMapping(value = "/{postId}", consumes = "multipart/form-data")
    public ApiResponse<?> updatePost(
            @LoginUserId int userId,
            @Valid @ModelAttribute PostReqDTO.updatePost request,
            @PathVariable Integer postId
    ) {
        postService.update(userId, postId, request);

        return ApiResponse.success("게시글 수정 성공", Map.of("postId", postId));
    }

    // 게시글 삭제
    @DeleteMapping(value = "/{postId}")
    public ApiResponse<?> deletePost(
            @LoginUserId int userId,
            @PathVariable Integer postId
    ) {
        postService.delete(userId, postId);

        return ApiResponse.success("게시글 삭제 성공", Map.of("postId", postId));
    }

    // 게시글 상세 조회
    @GetMapping(value = "/{postId}")
    public ApiResponse<?> getPost(@PathVariable Integer postId) {
        PostResDTO.postDetailResponse postDetail = postService.get(postId);

        return ApiResponse.success("게시글 상세 조회 성공", Map.of("post", postDetail));
    }

    // 게시글 목록 조회
    @GetMapping
    public ApiResponse<?> getPostList(
            @RequestParam(defaultValue = "0") Integer cursor,
            @RequestParam(defaultValue = "10") int size
    ) {
        PostResDTO.postListResponse postList = postService.getList(cursor, size);
        return ApiResponse.success("전체 게시글 목록 조회 성공", postList);
    }

    // 게시글 좋아요 토글
    @PostMapping("/{postId}/like")
    public ApiResponse<?> toggleLike(
            @LoginUserId int userId,
            @PathVariable Integer postId
    ) {
        PostResDTO.likeResult statusLike = postService.toggleLike(userId, postId);
        return ApiResponse.success("게시글 좋아요 처리 성공", statusLike);
    }
}
