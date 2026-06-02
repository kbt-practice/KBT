package com.kbt.amumal.domain.post.repository;

import com.kbt.amumal.domain.post.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<Like, Long> {
    boolean existsByUserIdAndPostId(String userId, Integer postId); // 유저+게시글 존재하는지 (좋아요 눌렀는지)
    void deleteByUserIdAndPostId(String userId, Integer postId); // 테이블에서 좋아요 삭제
    long countByPostId(Integer postId); // 게시글 좋아요 수
}