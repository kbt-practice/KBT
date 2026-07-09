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

    /**
     * 커서 기반으로 게시글 목록을 최신순(createdAt DESC)으로 조회한다.
     *
     * cursor가 0 또는 null이면 가장 최신 게시글부터 반환한다.
     * cursor가 있으면 해당 postId보다 작은(이전에 작성된) 게시글을 반환한다.
     *
     * @param cursor 직전 페이지의 마지막 postId (첫 페이지 요청 시 0 또는 null)
     * @param size   한 페이지에 가져올 게시글 수 (hasNext 판단을 위해 실제로는 size+1 전달)
     */
    @Override
    public List<Post> findPostsWithCursor(Integer cursor, int size) {
        return queryFactory
                .selectFrom(post)
                .where(
                        post.deletedAt.isNull(),
                        beforeCursor(cursor)
                )
                .orderBy(post.postId.desc())
                .limit(size)
                .fetch();
    }

    /**
     * 최신순 커서 조건: cursor보다 postId가 작은 게시글만 조회한다.
     * cursor가 없으면 조건 없이 전체 최신 글부터 조회한다.
     */
    private BooleanExpression beforeCursor(Integer cursor) {
        if (cursor == null || cursor == 0) return null;
        return post.postId.lt(cursor);
    }
}