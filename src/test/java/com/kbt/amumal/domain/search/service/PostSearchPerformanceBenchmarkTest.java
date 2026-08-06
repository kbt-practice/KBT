package com.kbt.amumal.domain.search.service;

import com.kbt.amumal.domain.post.dto.PostResDTO;
import com.kbt.amumal.domain.post.entity.Post;
import com.kbt.amumal.domain.post.repository.PostRepository;
import com.kbt.amumal.domain.search.document.PostDocument;
import com.kbt.amumal.domain.search.dto.PostSearchResDTO;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

// LIKE 풀스캔 검색 vs 실제 구현된 ES 검색(PostSearchService)의 쿼리 개수·소요시간을,
// 데이터 규모별로 반복 측정해 ES가 유리해지는 손익분기점을 찾기 위한 벤치마크
// search.enabled은 integration 프로필 기본값이 false라 이 테스트에서만 true로 켠다
@Slf4j
@Tag("integration")
@ActiveProfiles("integration")
@SpringBootTest(properties = "search.enabled=true")
class PostSearchPerformanceBenchmarkTest {

    private static final int KEYWORD_HIT_COUNT = 15;
    private static final int PAGE_SIZE = 20;
    // 형태소 분석/오타 허용 이슈와 무관하게 순수 색인 방식 성능만 비교하기 위한, 다른 글자와 안 섞이는 전용 키워드
    private static final String KEYWORD = "BENCHMARKTOKEN";

    private static final List<Row> summary = new ArrayList<>();

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostSearchService postSearchService;

    @Autowired
    private PostSearchIndexGateway indexGateway;

    @Autowired
    private PostDocumentFactory documentFactory;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    private List<Integer> keywordPostIds;
    private Statistics statistics;

    @BeforeEach
    void setUp() {
        statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.setStatisticsEnabled(true);
    }

    @AfterEach
    void tearDown() {
        postRepository.deleteAll();
        // 건별로 지우는 대신 인덱스를 통째로 지운다 (다음 테스트의 ensureIndex가 재생성)
        elasticsearchOperations.indexOps(PostDocument.class).delete();
    }

    @AfterAll
    static void printSummary() {
        log.info("=== LIKE vs ES 규모별 비교 요약 (post 건수 기준) ===");
        log.info(String.format("%10s | %10s(쿼리) | %10s(쿼리) | %s", "postCount", "LIKE ms", "ES ms", "더 빠른 쪽"));
        summary.stream()
                .sorted(Comparator.comparingInt(Row::postCount))
                .forEach(r -> log.info(String.format("%10d | %6dms(%2d) | %6dms(%2d) | %s",
                        r.postCount(), r.likeMs(), r.likeQueries(), r.esMs(), r.esQueries(),
                        r.esMs() < r.likeMs() ? "ES" : "LIKE")));
    }

    // 맨 앞의 1은 ES 클라이언트 콜드스타트(커넥션 풀 초기화 등) 비용을 흡수시키는 웜업용이라 요약 표에서는 제외한다
    private static final int WARMUP_POST_COUNT = 1;

    @ParameterizedTest(name = "post {0}건일 때 LIKE vs ES 비교")
    @ValueSource(ints = {WARMUP_POST_COUNT, 100, 500, 1_000, 3_000, 10_000, 30_000, 100_000})
    @DisplayName("키워드 검색 시 데이터 규모별로 LIKE 풀스캔과 ES 검색의 쿼리 개수·소요시간을 비교한다")
    void compareLikeAndElasticsearch(int postCount) {
        seedData(postCount);

        Result likeResult = measure("1. MySQL LIKE (%keyword%) 풀스캔", this::searchByLike);
        Result esResult = measure("2. Elasticsearch 검색 (PostSearchService)", this::searchByElasticsearch);

        // 두 방식 모두 키워드가 포함된 게시글을 빠짐없이 찾아야 한다 (정확성은 동일해야 비교 의미가 있음)
        assertThat(likeResult.postIds()).containsExactlyInAnyOrderElementsOf(keywordPostIds);
        assertThat(esResult.postIds()).containsExactlyInAnyOrderElementsOf(keywordPostIds);

        if (postCount == WARMUP_POST_COUNT) {
            log.info("=== post {}건(웜업, 요약 제외): LIKE {}ms(쿼리 {}개) vs ES {}ms(쿼리 {}개) ===",
                    postCount, likeResult.elapsedMs(), likeResult.queryCount(),
                    esResult.elapsedMs(), esResult.queryCount());
            return;
        }

        log.info("=== post {}건 비교 결과: LIKE {}ms(쿼리 {}개) vs ES {}ms(쿼리 {}개) ===",
                postCount, likeResult.elapsedMs(), likeResult.queryCount(),
                esResult.elapsedMs(), esResult.queryCount());

        summary.add(new Row(postCount, likeResult.elapsedMs(), likeResult.queryCount(),
                esResult.elapsedMs(), esResult.queryCount()));
    }

    private void seedData(int postCount) {
        log.info("더미데이터 세팅 시작: post {}개 (키워드 포함 {}개)", postCount, KEYWORD_HIT_COUNT);
        long seedStart = System.nanoTime();

        List<Post> newPosts = IntStream.range(0, postCount)
                .mapToObj(i -> Post.builder()
                        .title("일본 여행 후기 " + i)
                        .content(i < KEYWORD_HIT_COUNT
                                ? "이번 여행 정말 좋았어요 " + KEYWORD + " 다음에 또 가고 싶습니다 " + i
                                : "이번 여행 정말 좋았어요 별다른 특이사항은 없었습니다 " + i)
                        .userId(1)
                        .build())
                .toList();
        List<Post> posts = postRepository.saveAll(newPosts);
        keywordPostIds = posts.stream()
                .filter(post -> post.getContent().contains(KEYWORD))
                .map(Post::getPostId)
                .toList();

        // 색인은 실제 증분 동기화(SearchIndexSyncService)를 거치지 않고 직접 색인해 세팅 시간을 아낀다 (읽기 성능만 측정)
        indexGateway.ensureIndex();
        List<PostDocument> documents = posts.stream()
                .map(post -> documentFactory.create(post, List.of()))
                .toList();
        indexGateway.saveAll(documents);
        elasticsearchOperations.indexOps(PostDocument.class).refresh();

        long seedMs = (System.nanoTime() - seedStart) / 1_000_000;
        log.info("더미데이터 세팅 완료: post {}건, {}ms", posts.size(), seedMs);
    }

    private Set<Integer> searchByLike() {
        return postRepository.searchByTitleOrContentLike(KEYWORD, PageRequest.of(0, PAGE_SIZE)).stream()
                .map(Post::getPostId)
                .collect(Collectors.toSet());
    }

    private Set<Integer> searchByElasticsearch() {
        PostSearchResDTO.response response = postSearchService.search(KEYWORD, "latest", null, PAGE_SIZE);
        return response.posts().stream()
                .map(PostResDTO.postListItem::postId)
                .collect(Collectors.toSet());
    }

    private Result measure(String label, Supplier<Set<Integer>> action) {
        statistics.clear();
        long start = System.nanoTime();
        Set<Integer> postIds = action.get();
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        long queryCount = statistics.getQueryExecutionCount();
        log.info("[{}] queries={}, time={}ms, hits={}", label, queryCount, elapsedMs, postIds.size());

        return new Result(postIds, queryCount, elapsedMs);
    }

    private record Result(Set<Integer> postIds, long queryCount, long elapsedMs) {
    }

    private record Row(int postCount, long likeMs, long likeQueries, long esMs, long esQueries) {
    }
}
