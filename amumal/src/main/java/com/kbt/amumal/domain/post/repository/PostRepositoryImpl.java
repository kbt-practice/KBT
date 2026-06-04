package com.kbt.amumal.domain.post.repository;

import com.kbt.amumal.domain.post.entity.Post;
import com.kbt.amumal.domain.post.entity.QPost;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private static final QPost post = QPost.post;

    @Override
    public List<Post> findPostsWithCursor(Integer cursor, int size) {
        return queryFactory
                .selectFrom(post)
                .where(
                        post.deletedAt.isNull(),
                        afterCursor(cursor)
                )
                .orderBy(post.createdAt.asc())
                .limit(size)
                .fetch();
    }

    private BooleanExpression afterCursor(Integer cursor) {
        if (cursor == null || cursor == 0) return null;
        return post.postId.gt(cursor);
    }
}