package com.kbt.amumal.domain.post.repository;

import com.kbt.amumal.domain.post.entity.Post;

import java.util.List;

public interface PostRepositoryCustom {
    List<Post> findPostsWithCursor(Integer cursor, int size);
}
