package com.kbt.amumal.domain.comment.repository;

import com.kbt.amumal.domain.comment.entity.Comment;
import com.kbt.amumal.domain.post.dto.CountProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface commentRepository extends JpaRepository<Comment, Integer> {
    List<Comment> findByPostIdAndDeletedAtIsNullOrderByCreatedAtAsc(int postId);
    void deleteByPostId(int postId);
    void deleteByUserId(int userId);

    // N+1 방지: 여러 게시글의 댓글 수를 DTO Projection으로 한 번에 조회
    @Query("SELECT new com.kbt.amumal.domain.post.dto.CountProjection(c.postId, COUNT(c)) FROM Comment c WHERE c.postId IN :postIds AND c.deletedAt IS NULL GROUP BY c.postId")
    List<CountProjection> countsByPostIds(@Param("postIds") List<Integer> postIds);
}
