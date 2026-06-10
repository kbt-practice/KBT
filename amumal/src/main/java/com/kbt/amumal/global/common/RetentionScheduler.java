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

화/**
 * 소프트 딜리트된 데이터를 주기적으로 정리하는 스케줄러.
 *
 * 실행 주기: 매일 새벽 3시 (Asia/Seoul)
 *
 * 처리 항목:
 *   - 소프트 딜리트 후 30일 이상 경과한 게시글의 이미지 파일 삭제 (DB row는 유지)
 *   - 소프트 딜리트 후 7일 이상 경과한 유저를 관련 데이터(게시글, 댓글, 좋아요, 이미지)와 함께 하드 딜리트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RetentionScheduler {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final commentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final ImageHandler imageHandler;

    /**
     * 소프트 딜리트된 지 30일 이상 된 게시글의 이미지 파일을 디스크에서 삭제한다.
     *
     * DB row는 삭제하지 않고 postImageUrl 컬럼만 null로 초기화하여,
     * 이미 삭제된 파일을 다음 실행 시 중복으로 처리하지 않도록 한다.
     */
    @Transactional
    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    public void deleteExpiredPostImages() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        List<Post> posts = postRepository.findByDeletedAtIsNotNullAndDeletedAtBeforeAndPostImageUrlIsNotNull(cutoff);

        if (posts.isEmpty()) return;

        log.info("만료 게시글 이미지 삭제 시작 - {}개", posts.size());

        for (Post post : posts) {
            deleteImageSafely(post.getPostImageUrl());
            post.clearPostImage(); // postImageUrl을 null로 초기화해 다음 실행 시 중복 삭제 방지
        }

        log.info("만료 게시글 이미지 삭제 완료");
    }

    /**
     * 소프트 딜리트된 지 7일 이상 된 유저를 하드 딜리트한다.
     *
     * 단순 DELETE가 아니라 유저에 연결된 모든 데이터를 직접 제거한 뒤 유저를 삭제한다.
     * JPA cascade 설정 없이 직접 삭제하는 이유: 대용량 데이터를 한 트랜잭션에서
     * cascade로 처리하면 메모리·락 이슈가 발생할 수 있으므로 명시적으로 처리한다.
     */
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

    /**
     * 유저 1명을 다음 순서로 완전 삭제한다.
     *   1. 유저의 모든 게시글에 달린 댓글·좋아요 삭제
     *   2. 유저의 게시글 이미지 파일 삭제 후 게시글 row 삭제
     *   3. 다른 게시글에 남긴 댓글·좋아요 삭제
     *   4. 프로필 이미지 파일 삭제
     *   5. 유저 row 삭제
     *
     * 소프트 딜리트 여부와 관계없이 유저의 모든 게시글을 처리한다.
     *
     * @param user 하드 딜리트할 유저
     */
    private void hardDeleteUser(User user) {
        // 유저 게시글 정리: 댓글·좋아요 먼저 삭제 후 게시글 삭제
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

    /**
     * 이미지 파일 삭제를 시도하고, 실패해도 예외를 전파하지 않는다.
     *
     * 파일 하나의 삭제 실패가 전체 배치 작업을 중단시키지 않도록
     * 예외를 잡아 경고 로그만 남기고 계속 진행한다.
     *
     * @param imageUrl 삭제할 이미지 경로 (null이면 ImageHandler 내부에서 무시됨)
     */
    private void deleteImageSafely(String imageUrl) {
        try {
            imageHandler.delete(imageUrl);
        } catch (Exception e) {
            log.warn("이미지 파일 삭제 실패 (DB 삭제는 계속 진행): {}", imageUrl);
        }
    }
}
