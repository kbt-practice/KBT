package com.kbt.amumal.domain.post.repository;

import com.kbt.amumal.domain.post.dto.CountProjection;
import com.kbt.amumal.domain.post.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LikeRepository extends JpaRepository<Like, Long> {
    boolean existsByUserIdAndPostId(int userId, Integer postId); // 유저+게시글 존재하는지 (좋아요 눌렀는지)
    void deleteByUserIdAndPostId(int userId, Integer postId); // 테이블에서 좋아요 삭제
    long countByPostId(Integer postId); // 게시글 좋아요 수
    void deleteByPostId(Integer postId);

    // N+1 방지: 여러 게시글의 좋아요 수를 DTO Projection으로 한 번에 조회
    @Query("SELECT new com.kbt.amumal.domain.post.dto.CountProjection(l.postId, COUNT(l)) FROM Like l WHERE l.postId IN :postIds GROUP BY l.postId")
    List<CountProjection> countsByPostIds(@Param("postIds") List<Integer> postIds);

    // 유저 하드 딜리트 시 다른 게시글에 남긴 좋아요는 삭제하지 않고 익명화 (count는 그대로 유지됨)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Like l SET l.userId = null WHERE l.userId = :userId")
    void anonymizeByUserId(@Param("userId") int userId);
}