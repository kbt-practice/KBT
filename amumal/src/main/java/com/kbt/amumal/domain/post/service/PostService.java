package com.kbt.amumal.domain.post.service;

import com.kbt.amumal.domain.post.dto.PostReqDTO;
import com.kbt.amumal.domain.post.dto.PostResDTO;
import com.kbt.amumal.domain.post.entity.Post;
import com.kbt.amumal.domain.post.repository.PostRepository;
import com.kbt.amumal.domain.user.dto.UserResDTO;
import com.kbt.amumal.global.common.ImageHandler;
import com.kbt.amumal.global.error.CustomException;
import com.kbt.amumal.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Service
@Transactional
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final ImageHandler fileService;

    // 게시글 추가
    public int create(String userId, PostReqDTO.createPost request) throws IOException {
        String postImageUrl = null; // 이미지 없으면 null로 입력
        if (request.getPostImage() != null && !request.getPostImage().isEmpty()) {
            postImageUrl = fileService.save(request.getPostImage()); // 이미지 있으면 입력된 파일로 입력
        }

        Post newPost = postRepository.save(Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .postImageUrl(postImageUrl)
                .userId(userId)
                .build());

        return newPost.getPostId();
    }

    // 게시글 수정
    public void update(String userId, Integer postId, PostReqDTO.updatePost request) throws IOException {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "게시글을 찾을 수 없습니다."));

        if (!post.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN, "본인 게시글만 수정할 수 있습니다.");
        }

        // 들어온 값만 반영
        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            post.updateTitle(request.getTitle());
        }
        if (request.getContent() != null && !request.getContent().isBlank()) {
            post.updateContent(request.getContent());
        }
        if (request.getPostImage() != null && !request.getPostImage().isEmpty()) {
            String imageUrl = fileService.save(request.getPostImage());
            post.updatePostImage(imageUrl);
        }
    }

    // 게시글 삭제
    public void delete(String userId, Integer postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "게시글을 찾을 수 없습니다."));

        if (!post.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN, "본인 게시글만 삭제할 수 있습니다.");
        }

        post.softDelete();
    }

    // 게시글 조회
    public PostResDTO.postInfo get(Integer postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "게시글을 찾을 수 없습니다."));

        return PostResDTO.postInfo.from(post);
    }
}