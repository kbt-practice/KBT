package com.kbt.amumal.global.common;

import com.kbt.amumal.domain.comment.repository.commentRepository;
import com.kbt.amumal.domain.post.entity.Post;
import com.kbt.amumal.domain.post.repository.LikeRepository;
import com.kbt.amumal.domain.post.repository.PostRepository;
import com.kbt.amumal.domain.user.entity.User;
import com.kbt.amumal.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RetentionScheduler {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final commentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final ImageHandler imageHandler;

    // 소프트 딜리트된 지 30일 이상 된 게시글의 이미지 파일 삭제 (row는 유지)
    @Transactional
    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    public void deleteExpiredPostImages() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        List<Post> posts = postRepository.findByDeletedAtIsNotNullAndDeletedAtBeforeAndPostImageUrlIsNotNull(cutoff);

        if (posts.isEmpty()) return;

        log.info("만료 게시글 이미지 삭제 시작 - {}개", posts.size());

        for (Post post : posts) {
            deleteImageSafely(post.getPostImageUrl());
            post.clearPostImage(); // DB의 postImageUrl을 null로 초기화 (중복 삭제 방지)
        }

        log.info("만료 게시글 이미지 삭제 완료");
    }

    // 소프트 딜리트된 유저 7일 후 하드 딜리트
    @Transactional
    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    public void deleteExpiredUsers() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(7);
        List<User> expiredUsers = userRepository.findByDeletedAtIsNotNullAndDeletedAtBefore(cutoff);

        if (expiredUsers.isEmpty()) return;

        log.info("만료 유저 하드 딜리트 시작 - {}개", expiredUsers.size());

        for (User user : expiredUsers) {
            hardDeleteUser(user);
        }

        log.info("만료 유저 하드 딜리트 완료");
    }

    // 유저 하드 딜리트: 유저 게시글 전체 정리 → 댓글/좋아요 정리 → 프로필 이미지 → 유저
    private void hardDeleteUser(User user) {
        // 소프트 딜리트 여부 무관하게 유저의 모든 게시글 처리
        List<Post> userPosts = postRepository.findByUserId(user.getId());
        for (Post post : userPosts) {
            deleteImageSafely(post.getPostImageUrl());
            commentRepository.deleteByPostId(post.getPostId());
            likeRepository.deleteByPostId(post.getPostId());
        }
        postRepository.deleteAll(userPosts);

        // 다른 게시글에 남긴 댓글, 좋아요 삭제
        commentRepository.deleteByUserId(user.getId());
        likeRepository.deleteByUserId(user.getId());

        deleteImageSafely(user.getProfileImageUrl());
        userRepository.delete(user);
    }

    // 파일 삭제 실패 시 로그만 남기고 계속 진행 (한 파일 실패가 전체를 막으면 안 됨)
    private void deleteImageSafely(String imageUrl) {
        try {
            imageHandler.delete(imageUrl);
        } catch (Exception e) {
            log.warn("이미지 파일 삭제 실패 (DB 삭제는 계속 진행): {}", imageUrl);
        }
    }
}
