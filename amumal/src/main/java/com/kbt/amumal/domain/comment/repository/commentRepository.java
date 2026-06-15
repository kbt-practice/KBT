package com.kbt.amumal.domain.comment.repository;

import com.kbt.amumal.domain.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface commentRepository extends JpaRepository<Comment, Integer> {
    long countByPostIdAndDeletedAtIsNull(int postId);
    List<Comment> findByPostIdAndDeletedAtIsNullOrderByCreatedAtAsc(int postId);
    void deleteByPostId(int postId);
    void deleteByUserId(int userId);
}
