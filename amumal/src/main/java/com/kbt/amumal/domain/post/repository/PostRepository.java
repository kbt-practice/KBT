package com.kbt.amumal.domain.post.repository;

import com.kbt.amumal.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Integer>, PostRepositoryCustom {
    List<Post> findByDeletedAtIsNotNullAndDeletedAtBeforeAndPostImageUrlIsNotNull(LocalDateTime cutoff);
    List<Post> findByUserId(int userId);
}