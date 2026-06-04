package com.kbt.amumal.domain.post.service;

import com.kbt.amumal.domain.comment.repository.commentRepository;
import com.kbt.amumal.domain.post.dto.PostReqDTO;
import com.kbt.amumal.domain.post.dto.PostResDTO;
import com.kbt.amumal.domain.post.entity.Like;
import com.kbt.amumal.domain.post.entity.Post;
import com.kbt.amumal.domain.post.repository.LikeRepository;
import com.kbt.amumal.domain.post.repository.PostRepository;
import com.kbt.amumal.domain.user.entity.User;
import com.kbt.amumal.domain.user.repository.UserRepository;
import com.kbt.amumal.global.common.ImageHandler;
import com.kbt.amumal.global.error.CustomException;
import com.kbt.amumal.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final LikeRepository likeRepository;
    private final commentRepository commentRepository;
    private final UserRepository userRepository;
    private final ImageHandler fileService;

    // 게시글 추가
    public int create(int id, PostReqDTO.createPost request) throws IOException {
        String postImageUrl = null;
        if (request.getPostImage() != null && !request.getPostImage().isEmpty()) {
            postImageUrl = fileService.postSave(request.getPostImage());
        }

        Post newPost = postRepository.save(Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .postImageUrl(postImageUrl)
                .userId(id)
                .build());

        return newPost.getPostId();
    }

    // 게시글 수정
    public void update(int id, Integer postId, PostReqDTO.updatePost request) throws IOException {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "게시글 정보를 확인해주세요."));

        if (post.getUserId() != id) {
            throw new CustomException(ErrorCode.FORBIDDEN, "본인 게시글만 수정할 수 있습니다.");
        }

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            post.updateTitle(request.getTitle());
        }
        if (request.getContent() != null && !request.getContent().isBlank()) {
            post.updateContent(request.getContent());
        }
        if (request.getPostImage() != null && !request.getPostImage().isEmpty()) {
            String imageUrl = fileService.postSave(request.getPostImage());
            post.updatePostImage(imageUrl);
        }
    }

    // 게시글 삭제
    public void delete(int id, Integer postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "게시글 정보를 확인해주세요."));

        if (post.getUserId() != id) {
            throw new CustomException(ErrorCode.FORBIDDEN, "본인 게시글만 삭제할 수 있습니다.");
        }

        post.softDelete();
    }

    // 게시글 상세 조회
    public PostResDTO.postDetailResponse get(Integer postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "게시글 정보를 확인해주세요."));

        if (post.getDeletedAt() != null) {
            throw new CustomException(ErrorCode.NOT_FOUND, "게시글 정보를 확인해주세요.");
        }

        post.incrementViewCount();

        User author = userRepository.findById(post.getUserId()).orElse(null);
        long likeCount = likeRepository.countByPostId(post.getPostId());

        List<PostResDTO.commentItem> commentItems = commentRepository
                .findByPostIdAndDeletedAtIsNullOrderByCreatedAtAsc(post.getPostId())
                .stream()
                .map(comment -> {
                    User commenter = userRepository.findById(comment.getUserId()).orElse(null);
                    return PostResDTO.commentItem.builder()
                            .commentId(comment.getCommentId())
                            .comment(comment.getContent())
                            .user(commenter != null ? PostResDTO.userInfo.builder()
                                    .userId(commenter.getUserId())
                                    .nickname(commenter.getNickname())
                                    .profileImage(commenter.getProfileImageUrl())
                                    .build() : null)
                            .createdAt(comment.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());

        return PostResDTO.postDetailResponse.builder()
                .postId(post.getPostId())
                .title(post.getTitle())
                .content(post.getContent())
                .like(likeCount)
                .view(post.getViewCount())
                .user(author != null ? PostResDTO.userInfo.builder()
                        .userId(author.getUserId())
                        .nickname(author.getNickname())
                        .profileImage(author.getProfileImageUrl())
                        .build() : null)
                .createdAt(post.getCreatedAt())
                .comments(commentItems)
                .build();
    }

    // 게시글 목록 조회 (커서 기반 페이지네이션)
    public PostResDTO.postListResponse getList(Integer cursor, int size) {
        List<Post> posts = postRepository.findPostsWithCursor(cursor, size + 1);

        boolean hasNext = posts.size() > size;
        if (hasNext) posts = posts.subList(0, size);

        Integer nextCursor = hasNext ? posts.get(posts.size() - 1).getPostId() : null;

        List<PostResDTO.postListItem> items = posts.stream().map(post -> {
            User user = userRepository.findById(post.getUserId()).orElse(null);
            long likeCount = likeRepository.countByPostId(post.getPostId());
            long commentCount = commentRepository.countByPostIdAndDeletedAtIsNull(post.getPostId());

            return PostResDTO.postListItem.builder()
                    .postId(post.getPostId())
                    .title(post.getTitle())
                    .like(likeCount)
                    .comment(commentCount)
                    .view(post.getViewCount())
                    .user(user != null ? PostResDTO.userInfo.builder()
                            .userId(user.getUserId())
                            .nickname(user.getNickname())
                            .profileImage(user.getProfileImageUrl())
                            .build() : null)
                    .createdAt(post.getCreatedAt())
                    .build();
        }).collect(Collectors.toList());

        return PostResDTO.postListResponse.builder()
                .posts(items)
                .pagination(PostResDTO.pagination.builder()
                        .nextCursor(nextCursor)
                        .hasNext(hasNext)
                        .build())
                .build();
    }

    // 게시글 좋아요 토글
    public PostResDTO.likeResult toggleLike(int id, Integer postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "게시글 정보를 확인해주세요."));

        if (post.getDeletedAt() != null)
            throw new CustomException(ErrorCode.NOT_FOUND, "게시글 정보를 확인해주세요.");

        User user = userRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "유저 정보를 확인해주세요."));

        String type;
        if (likeRepository.existsByUserIdAndPostId(id, postId)) {
            likeRepository.deleteByUserIdAndPostId(id, postId);
            type = "DELETE";
        } else {
            likeRepository.save(Like.builder().userId(id).postId(postId).build());
            type = "CREATE";
        }

        return PostResDTO.likeResult.builder()
                .userId(user.getUserId())
                .postId(postId)
                .type(type)
                .build();
    }
}