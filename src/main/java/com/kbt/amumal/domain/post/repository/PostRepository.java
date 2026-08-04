package com.kbt.amumal.domain.post.repository;

import com.kbt.amumal.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Integer>, PostRepositoryCustom {
    List<Post> findByDeletedAtIsNotNullAndDeletedAtBeforeAndPostImageUrlIsNotNull(LocalDateTime cutoff);
    List<Post> findByUserId(int userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.postId = :postId")
    void incrementViewCount(@Param("postId") Integer postId);

    // 좋아요 수 비정규화: Like 테이블 COUNT 대신 Post에 저장된 값을 +1/-1로 원자적으로 갱신
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Post p SET p.likeCount = p.likeCount + 1 WHERE p.postId = :postId")
    void incrementLikeCount(@Param("postId") Integer postId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Post p SET p.likeCount = p.likeCount - 1 WHERE p.postId = :postId AND p.likeCount > 0")
    void decrementLikeCount(@Param("postId") Integer postId);

    // 댓글 수 비정규화: Comment 테이블 COUNT 대신 Post에 저장된 값을 +1/-1로 원자적으로 갱신
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Post p SET p.commentCount = p.commentCount + 1 WHERE p.postId = :postId")
    void incrementCommentCount(@Param("postId") Integer postId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Post p SET p.commentCount = p.commentCount - 1 WHERE p.postId = :postId AND p.commentCount > 0")
    void decrementCommentCount(@Param("postId") Integer postId);
}