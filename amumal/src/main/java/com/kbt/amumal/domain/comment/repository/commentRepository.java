package com.kbt.amumal.domain.comment.repository;

import com.kbt.amumal.domain.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface commentRepository extends JpaRepository<Comment, Integer> {
}
