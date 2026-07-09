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
}