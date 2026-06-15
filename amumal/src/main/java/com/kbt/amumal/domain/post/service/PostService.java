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

    public int create(int id, PostReqDTO.createPost request, MultipartFile postImage) {
        String postImageUrl = uploadImageIfPresent(postImage);

        registerImageRollbackOnFailure(postImageUrl);

        Post newPost = postRepository.save(Post.builder()
                .title(request.title())
                .content(request.content())
                .postImageUrl(postImageUrl)
                .userId(id)
                .build());

        return newPost.getPostId();
    }

    public void update(int id, Integer postId, PostReqDTO.updatePost request, MultipartFile postImage) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        if (post.getUserId() != id)
            throw new CustomException(ErrorCode.POST_FORBIDDEN_UPDATE);

        if (request.title() != null && !request.title().isBlank())
            post.updateTitle(request.title());
        if (request.content() != null && !request.content().isBlank())
            post.updateContent(request.content());

        if (postImage != null && !postImage.isEmpty()) {
            String oldImageUrl = post.getPostImageUrl();
            String newImageUrl = fileService.postSave(postImage);

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
                    return new PostResDTO.commentItem(
                            comment.getCommentId(),
                            comment.getContent(),
                            commenter != null ? new PostResDTO.userInfo(commenter.getUserId(), commenter.getNickname(), commenter.getProfileImageUrl()) : null,
                            comment.getCreatedAt()
                    );
                })
                .collect(Collectors.toList());

        return new PostResDTO.postDetailResponse(
                post.getPostId(),
                post.getTitle(),
                post.getContent(),
                post.getPostImageUrl(),
                likeCount,
                post.getViewCount(),
                author != null ? new PostResDTO.userInfo(author.getUserId(), author.getNickname(), author.getProfileImageUrl()) : null,
                post.getCreatedAt(),
                commentItems
        );
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

            return new PostResDTO.postListItem(
                    post.getPostId(),
                    post.getTitle(),
                    post.getPostImageUrl(),
                    likeCount,
                    commentCount,
                    post.getViewCount(),
                    user != null ? new PostResDTO.userInfo(user.getUserId(), user.getNickname(), user.getProfileImageUrl()) : null,
                    post.getCreatedAt()
            );
        }).collect(Collectors.toList());

        return new PostResDTO.postListResponse(items, new PostResDTO.pagination(nextCursor, hasNext));
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

        return new PostResDTO.likeResult(user.getUserId(), postId, type);
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
