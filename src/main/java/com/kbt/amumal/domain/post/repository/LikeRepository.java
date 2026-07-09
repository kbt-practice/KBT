package com.kbt.amumal.domain.post.repository;

import com.kbt.amumal.domain.post.dto.CountProjection;
import com.kbt.amumal.domain.post.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LikeRepository extends JpaRepository<Like, Long> {
    boolean existsByUserIdAndPostId(int userId, Integer postId); // 유저+게시글 존재하는지 (좋아요 눌렀는지)
    void deleteByUserIdAndPostId(int userId, Integer postId); // 테이블에서 좋아요 삭제
    long countByPostId(Integer postId); // 게시글 좋아요 수
    void deleteByPostId(Integer postId);
    void deleteByUserId(int userId);

    // N+1 방지: 여러 게시글의 좋아요 수를 DTO Projection으로 한 번에 조회
    @Query("SELECT new com.kbt.amumal.domain.post.dto.CountProjection(l.postId, COUNT(l)) FROM Like l WHERE l.postId IN :postIds GROUP BY l.postId")
    List<CountProjection> countsByPostIds(@Param("postIds") List<Integer> postIds);
}