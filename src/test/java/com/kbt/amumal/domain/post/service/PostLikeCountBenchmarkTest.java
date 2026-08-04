package com.kbt.amumal.domain.post.service;

import com.kbt.amumal.domain.comment.entity.Comment;
import com.kbt.amumal.domain.comment.repository.commentRepository;
import com.kbt.amumal.domain.post.dto.CountProjection;
import com.kbt.amumal.domain.post.entity.Like;
import com.kbt.amumal.domain.post.entity.Post;
import com.kbt.amumal.domain.post.repository.LikeRepository;
import com.kbt.amumal.domain.post.repository.PostRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

// 좋아요+댓글 수를 함께 조회하는 방식 3가지(Projection / 일반 카운트 / 비정규화) 쿼리 개수·소요시간 비교
// 좋아요만 Post.likeCount로 비정규화되어 있고, 댓글은 실제 코드에 반영하지 않아 그대로 쿼리로 센다 (테스트 코드 한정)
@Slf4j
@Tag("integration")
@ActiveProfiles("integration")
@SpringBootTest
class PostLikeCountBenchmarkTest {

    private static final int POST_COUNT = 200;
    private static final int LIKES_PER_POST = 50;
    private static final int COMMENTS_PER_POST = 20;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private commentRepository commentRepository;

    private List<Post> posts; // getList()에서 이미 로딩된 상태를 흉내내기 위해 보관 (비정규화 방식이 재조회하지 않도록)
    private List<Integer> postIds;
    private Statistics statistics;

    @BeforeEach
    void setUp() {
        statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.setStatisticsEnabled(true);

        log.info("더미데이터 세팅 시작: post {}개 x like {}개 x comment {}개", POST_COUNT, LIKES_PER_POST, COMMENTS_PER_POST);
        long seedStart = System.nanoTime();

        // 좋아요 수(likeCount)는 실제 운영에서는 increment/decrementLikeCount로 누적되지만,
        // 벤치마크 세팅에서는 read 성능만 재면 되므로 최종값을 바로 저장해 세팅 시간을 아낀다
        List<Post> newPosts = IntStream.range(0, POST_COUNT)
                .mapToObj(i -> Post.builder()
                        .title("title-" + i)
                        .content("content-" + i)
                        .userId(1)
                        .likeCount(LIKES_PER_POST)
                        .build())
                .toList();
        posts = postRepository.saveAll(newPosts);
        postIds = posts.stream().map(Post::getPostId).toList();

        List<Like> likes = new ArrayList<>(POST_COUNT * LIKES_PER_POST);
        List<Comment> comments = new ArrayList<>(POST_COUNT * COMMENTS_PER_POST);
        for (Integer postId : postIds) {
            for (int i = 0; i < LIKES_PER_POST; i++) {
                likes.add(Like.builder().userId(i + 1).postId(postId).build());
            }
            for (int i = 0; i < COMMENTS_PER_POST; i++) {
                comments.add(Comment.builder().content("comment-" + i).userId(i + 1).postId(postId).build());
            }
        }
        likeRepository.saveAll(likes);
        commentRepository.saveAll(comments);

        long seedMs = (System.nanoTime() - seedStart) / 1_000_000;
        log.info("더미데이터 세팅 완료: post {}건, like {}건, comment {}건, {}ms",
                posts.size(), likes.size(), comments.size(), seedMs);
    }

    @AfterEach
    void tearDown() {
        commentRepository.deleteAll();
        likeRepository.deleteAll();
        postRepository.deleteAll();
    }

    @Test
    @DisplayName("좋아요+댓글 수를 함께 조회할 때 Projection / 일반 카운트 / 비정규화 방식의 쿼리 개수·소요시간을 비교한다")
    void compareThreeApproaches() {
        Result projection = measure("1. DTO Projection (좋아요+댓글 각각 GROUP BY)", this::byProjection);
        Result naive = measure("2. 일반 카운트 조회 (postId 별 좋아요+댓글 count, N+1)", this::byNaiveCount);
        Result denormalized = measure("3. 비정규화 (좋아요는 Post.likeCount, 댓글은 Projection)", this::byDenormalizedColumn);

        // 값 자체는 세 방식 모두 동일해야 한다
        assertThat(projection.counts()).isEqualTo(naive.counts());
        assertThat(projection.counts()).isEqualTo(denormalized.counts());

        // N+1 방식만 postId 개수 x 2(좋아요+댓글)만큼 쿼리가 나가야 한다
        assertThat(naive.queryCount()).isEqualTo(POST_COUNT * 2L);
        assertThat(projection.queryCount()).isLessThan(naive.queryCount());
        assertThat(denormalized.queryCount()).isLessThan(projection.queryCount());
    }

    private Map<Integer, PostCounts> byProjection() {
        Map<Integer, Long> likeCounts = likeRepository.countsByPostIds(postIds).stream()
                .collect(Collectors.toMap(CountProjection::postId, CountProjection::count));
        Map<Integer, Long> commentCounts = commentRepository.countsByPostIds(postIds).stream()
                .collect(Collectors.toMap(CountProjection::postId, CountProjection::count));
        return mergeCounts(likeCounts, commentCounts);
    }

    private Map<Integer, PostCounts> byNaiveCount() {
        Map<Integer, Long> likeCounts = postIds.stream()
                .collect(Collectors.toMap(id -> id, likeRepository::countByPostId));
        Map<Integer, Long> commentCounts = postIds.stream()
                .collect(Collectors.toMap(id -> id, this::countCommentsNaive));
        return mergeCounts(likeCounts, commentCounts);
    }

    private Map<Integer, PostCounts> byDenormalizedColumn() {
        // 좋아요: 목록 조회 시 이미 로딩된 Post에서 바로 읽음 (추가 쿼리 없음)
        Map<Integer, Long> likeCounts = posts.stream()
                .collect(Collectors.toMap(Post::getPostId, post -> (long) post.getLikeCount()));
        // 댓글: 비정규화 컬럼이 없어 Projection으로 조회
        Map<Integer, Long> commentCounts = commentRepository.countsByPostIds(postIds).stream()
                .collect(Collectors.toMap(CountProjection::postId, CountProjection::count));
        return mergeCounts(likeCounts, commentCounts);
    }

    private long countCommentsNaive(Integer postId) {
        return entityManager.createQuery(
                        "SELECT COUNT(c) FROM Comment c WHERE c.postId = :postId AND c.deletedAt IS NULL", Long.class)
                .setParameter("postId", postId)
                .getSingleResult();
    }

    private Map<Integer, PostCounts> mergeCounts(Map<Integer, Long> likeCounts, Map<Integer, Long> commentCounts) {
        return postIds.stream().collect(Collectors.toMap(
                id -> id,
                id -> new PostCounts(likeCounts.getOrDefault(id, 0L), commentCounts.getOrDefault(id, 0L))
        ));
    }

    private Result measure(String label, Supplier<Map<Integer, PostCounts>> action) {
        statistics.clear();
        long start = System.nanoTime();
        Map<Integer, PostCounts> counts = action.get();
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        long queryCount = statistics.getQueryExecutionCount();
        log.info("[{}] queries={}, time={}ms", label, queryCount, elapsedMs);

        return new Result(counts, queryCount, elapsedMs);
    }

    private record PostCounts(long likeCount, long commentCount) {
    }

    private record Result(Map<Integer, PostCounts> counts, long queryCount, long elapsedMs) {
    }
}
