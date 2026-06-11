package com.kbt.amumal.domain.post.controller;

import com.kbt.amumal.domain.post.dto.PostReqDTO;
import com.kbt.amumal.domain.post.dto.PostResDTO;
import com.kbt.amumal.domain.post.service.PostService;
import com.kbt.amumal.global.common.ApiResponse;
import com.kbt.amumal.global.interceptor.LoginUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "게시글", description = "게시글 CRUD·좋아요 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {
    private final PostService postService;

    @Operation(summary = "게시글 등록", description = "제목·내용·이미지(선택)로 게시글을 생성합니다.")
    @PostMapping(value = "/", consumes = "multipart/form-data")
    public ApiResponse<?> createPost(@LoginUserId int userId, @Valid @ModelAttribute PostReqDTO.createPost request) {
        int newPostId = postService.create(userId, request);

        return ApiResponse.success("게시글 생성 성공", Map.of("postId", newPostId));
    }

    @Operation(summary = "게시글 수정", description = "전달된 필드만 업데이트합니다.")
    @PatchMapping(value = "/{postId}", consumes = "multipart/form-data")
    public ApiResponse<?> updatePost(
            @LoginUserId int userId,
            @Valid @ModelAttribute PostReqDTO.updatePost request,
            @Parameter(description = "수정할 게시글 ID") @PathVariable Integer postId
    ) {
        postService.update(userId, postId, request);

        return ApiResponse.success("게시글 수정 성공", Map.of("postId", postId));
    }

    @Operation(summary = "게시글 삭제", description = "소프트 딜리트합니다. 30일 후 이미지가 삭제됩니다.")
    @DeleteMapping(value = "/{postId}")
    public ApiResponse<?> deletePost(
            @LoginUserId int userId,
            @Parameter(description = "삭제할 게시글 ID") @PathVariable Integer postId
    ) {
        postService.delete(userId, postId);

        return ApiResponse.success("게시글 삭제 성공", Map.of("postId", postId));
    }

    @Operation(summary = "게시글 상세 조회", description = "조회 시 조회수가 1 증가합니다.")
    @SecurityRequirements
    @GetMapping(value = "/{postId}")
    public ApiResponse<?> getPost(
            @Parameter(description = "조회할 게시글 ID") @PathVariable Integer postId
    ) {
        PostResDTO.postDetailResponse postDetail = postService.get(postId);

        return ApiResponse.success("게시글 상세 조회 성공", Map.of("post", postDetail));
    }

    @Operation(summary = "게시글 목록 조회", description = "커서 기반 페이지네이션. cursor=0 이면 첫 페이지.")
    @SecurityRequirements
    @GetMapping
    public ApiResponse<?> getPostList(
            @Parameter(description = "마지막으로 받은 게시글 ID (첫 페이지는 0)") @RequestParam(defaultValue = "0") Integer cursor,
            @Parameter(description = "한 번에 가져올 게시글 수") @RequestParam(defaultValue = "10") int size
    ) {
        PostResDTO.postListResponse postList = postService.getList(cursor, size);
        return ApiResponse.success("전체 게시글 목록 조회 성공", postList);
    }

    @Operation(summary = "게시글 좋아요 토글", description = "좋아요가 없으면 추가, 있으면 취소합니다.")
    @PostMapping("/{postId}/like")
    public ApiResponse<?> toggleLike(
            @LoginUserId int userId,
            @Parameter(description = "좋아요를 토글할 게시글 ID") @PathVariable Integer postId
    ) {
        PostResDTO.likeResult statusLike = postService.toggleLike(userId, postId);
        return ApiResponse.success("게시글 좋아요 처리 성공", statusLike);
    }
}
