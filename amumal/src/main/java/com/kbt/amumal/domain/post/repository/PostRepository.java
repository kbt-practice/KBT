package com.kbt.amumal.domain.post.repository;

import com.kbt.amumal.domain.post.entity.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Integer> {
    List<Post> findByDeletedAtIsNullAndPostIdGreaterThanOrderByCreatedAtAsc(Integer cursor, Pageable pageable); // 삭제되지 않은 게시글, 생성순
}