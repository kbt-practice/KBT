package com.kbt.amumal.domain.post.service;

import com.kbt.amumal.domain.comment.entity.Comment;
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
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
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

    public int create(int id, PostReqDTO.createPost request) {
        String postImageUrl = uploadImageIfPresent(request.getPostImage());

        registerImageRollbackOnFailure(postImageUrl);

        Post newPost = postRepository.save(Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .postImageUrl(postImageUrl)
                .userId(id)
                .build());

        return newPost.getPostId();
    }

    public void update(int id, Integer postId, PostReqDTO.updatePost request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        if (post.getUserId() != id)
            throw new CustomException(ErrorCode.POST_FORBIDDEN_UPDATE);

        if (request.getTitle() != null && !request.getTitle().isBlank())
            post.updateTitle(request.getTitle());
        if (request.getContent() != null && !request.getContent().isBlank())
            post.updateContent(request.getContent());

        if (request.getPostImage() != null && !request.getPostImage().isEmpty()) {
            String oldImageUrl = post.getPostImageUrl();
            String newImageUrl = fileService.postSave(request.getPostImage());

            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    if (status == TransactionSynchronization.STATUS_COMMITTED) {
                        fileService.deleteSafely(oldImageUrl);
                    } else if (status == TransactionSynchronization.STATUS_ROLLED_BACK) {
                        fileService.deleteSafely(newImageUrl);
                    }
                }
            });

            post.updatePostImage(newImageUrl);
        }
    }

    public void delete(int id, Integer postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        if (post.getUserId() != id)
            throw new CustomException(ErrorCode.POST_FORBIDDEN_DELETE);

        post.softDelete();
    }

    public PostResDTO.postDetailResponse get(Integer postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        if (post.getDeletedAt() != null)
            throw new CustomException(ErrorCode.POST_ALREADY_DELETED);

        postRepository.incrementViewCount(postId); // clearAutomatically = true → L1 캐시 초기화
        post = postRepository.findById(postId).orElseThrow(); // 증가된 viewCount 반영

        User author = userRepository.findById(post.getUserId()).orElse(null);
        long likeCount = likeRepository.countByPostId(post.getPostId());

        List<Comment> comments = commentRepository
                .findByPostIdAndDeletedAtIsNullOrderByCreatedAtAsc(post.getPostId());

        // 댓글 작성자 ID 목록으로 한 번에 조회 (N+1 방지)
        List<Integer> commenterIds = comments.stream()
                .map(Comment::getUserId)
                .distinct()
                .collect(Collectors.toList());
        Map<Integer, User> commenterMap = userRepository.findAllById(commenterIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        List<PostResDTO.commentItem> commentItems = comments.stream()
                .map(comment -> {
                    User commenter = commenterMap.get(comment.getUserId());
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

    public PostResDTO.postListResponse getList(Integer cursor, int size) {
        List<Post> posts = postRepository.findPostsWithCursor(cursor, size + 1);

        boolean hasNext = posts.size() > size;
        if (hasNext) posts = posts.subList(0, size);

        Integer nextCursor = hasNext ? posts.get(posts.size() - 1).getPostId() : null;

        // 게시글 작성자 ID 목록으로 한 번에 조회 (N+1 방지)
        List<Integer> authorIds = posts.stream()
                .map(Post::getUserId)
                .distinct()
                .collect(Collectors.toList());
        Map<Integer, User> authorMap = userRepository.findAllById(authorIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        List<PostResDTO.postListItem> items = posts.stream().map(post -> {
            User user = authorMap.get(post.getUserId());
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

    public PostResDTO.likeResult toggleLike(int id, Integer postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        if (post.getDeletedAt() != null)
            throw new CustomException(ErrorCode.POST_ALREADY_DELETED);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

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

    private String uploadImageIfPresent(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;
        return fileService.postSave(file);
    }

    private void registerImageRollbackOnFailure(String imageUrl) {
        if (imageUrl == null) return;
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == TransactionSynchronization.STATUS_ROLLED_BACK) {
                    fileService.deleteSafely(imageUrl);
                }
            }
        });
    }
}
