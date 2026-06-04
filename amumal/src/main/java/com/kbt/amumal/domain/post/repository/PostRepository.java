package com.kbt.amumal.domain.post.repository;

import com.kbt.amumal.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Integer>, PostRepositoryCustom {
}