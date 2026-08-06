package com.kbt.amumal.domain.search.service;

import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.kbt.amumal.domain.post.dto.PostResDTO;
import com.kbt.amumal.domain.post.entity.Post;
import com.kbt.amumal.domain.post.repository.PostRepository;
import com.kbt.amumal.domain.search.document.PostDocument;
import com.kbt.amumal.domain.search.dto.PostSearchResDTO;
import com.kbt.amumal.domain.search.dto.SearchSortType;
import com.kbt.amumal.domain.user.dto.UserProjection;
import com.kbt.amumal.domain.user.repository.UserRepository;
import com.kbt.amumal.global.error.CustomException;
import com.kbt.amumal.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "search.enabled", havingValue = "true")
public class PostSearchService {

    private final ElasticsearchOperations operations;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    // 필드별 점수 가중치 (제목 > 본문 > 댓글 순으로 점수를 더 준다)
    @Value("${search.boost.title:4.0}")
    private float titleBoost;
    @Value("${search.boost.content:2.0}")
    private float contentBoost;
    @Value("${search.boost.comments:1.0}")
    private float commentsBoost;

    // 오타 허용(fuzzy) 설정 - nori가 형태소 단위로 쪼갠 토큰을 기준으로 몇 글자까지 다르게 입력해도 매칭시킬지 결정한다
    @Value("${search.fuzziness:AUTO}") // AUTO = 토큰 길이에 따라 자동으로 허용 오차(0~2) 조정
    private String fuzziness;
    @Value("${search.fuzzy.max-expansions:25}") // fuzzy 매칭 후보로 몇 개 토큰까지 확장 탐색할지 (많을수록 느려짐)
    private int maxExpansions;
    @Value("${search.fuzzy.prefix-length:0}") // 앞에서부터 몇 글자는 오타 허용 없이 정확히 일치해야 하는지
    private int prefixLength;

    // 검색 요청의 입구: 입력값 검증 → ES 쿼리 실행 → 다음 페이지 존재 여부·커서 계산 → DB 하이드레이션까지 총괄
    @Transactional(readOnly = true)
    public PostSearchResDTO.response search(
            String keyword,
            String sortValue,
            String cursor,
            int size
    ) {
        if (keyword == null || keyword.isBlank()) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "검색어를 입력해주세요.");
        }
        if (size < 1 || size > 100) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "size는 1에서 100 사이여야 합니다.");
        }

        SearchSortType sortType = parseSortType(sortValue);
        NativeQuery query = buildQuery(keyword.trim(), sortType, cursor, size + 1);
        SearchHits<PostDocument> searchHits = operations.search(query, PostDocument.class);

        List<SearchHit<PostDocument>> hits = searchHits.getSearchHits();
        boolean hasNext = hits.size() > size;
        List<SearchHit<PostDocument>> pageHits = hasNext ? hits.subList(0, size) : hits;
        String nextCursor = hasNext && !pageHits.isEmpty()
                ? encodeCursor(pageHits.get(pageHits.size() - 1).getSortValues())
                : null;

        return new PostSearchResDTO.response(
                hydratePostCards(pageHits),
                new PostSearchResDTO.pagination(nextCursor, hasNext)
        );
    }

    // nori 분석 + fuzzy가 적용된 multiMatch 쿼리와 정렬·커서 조건을 조립해 ES 네이티브 쿼리를 만든다
    private NativeQuery buildQuery(String keyword, SearchSortType sortType, String cursor, int limit) {
        // "^숫자" 문법으로 필드별 가중치를 ES에 전달 (예: "title^4.0")
        List<String> fields = List.of(
                "title^" + titleBoost,
                "content^" + contentBoost,
                "comments^" + commentsBoost
        );

        NativeQueryBuilder builder = NativeQuery.builder()
                // multiMatch: 입력한 keyword를 nori로 분석한 뒤(색인 때와 동일한 korean_nori analyzer 사용) title/content/comments에서 동시에 검색
                .withQuery(query -> query.multiMatch(multiMatch -> multiMatch
                        .query(keyword)
                        .type(TextQueryType.BestFields) // 필드 중 가장 잘 맞은 것의 점수를 기준으로 채택 (합산 아님)
                        .fields(fields)
                        .fuzziness(fuzziness) // 여기서 오타 허용(fuzzy)이 실제로 적용됨
                        .maxExpansions(maxExpansions)
                        .prefixLength(prefixLength)
                        .tieBreaker(0.2) // 채택되지 않은 필드 점수도 20%만큼 더해 동점 상황을 보정
                ))
                .withPageable(PageRequest.of(0, limit));

        if (sortType == SearchSortType.ACCURACY) {
            builder.withSort(sort -> sort.score(score -> score.order(SortOrder.Desc)))
                    .withSort(sort -> sort.field(field -> field.field("createdAt").order(SortOrder.Desc)))
                    .withSort(sort -> sort.field(field -> field.field("postId").order(SortOrder.Desc)));
        } else {
            builder.withSort(sort -> sort.field(field -> field.field("createdAt").order(SortOrder.Desc)))
                    .withSort(sort -> sort.field(field -> field.field("postId").order(SortOrder.Desc)));
        }

        if (cursor != null && !cursor.isBlank()) {
            builder.withSearchAfter(decodeCursor(cursor));
        }
        return builder.build();
    }

    // ES 검색결과(postId 목록)로 MySQL에서 게시글 본문·작성자 정보를 가져와 화면에 필요한 카드 형태로 조립
    private List<PostResDTO.postListItem> hydratePostCards(List<SearchHit<PostDocument>> hits) {
        List<Integer> orderedIds = hits.stream()
                .map(hit -> hit.getContent().getPostId())
                .toList();
        if (orderedIds.isEmpty()) return List.of();

        Map<Integer, Post> postMap = postRepository.findAllById(orderedIds).stream()
                .filter(post -> post.getDeletedAt() == null)
                .collect(Collectors.toMap(Post::getPostId, Function.identity()));
        List<Integer> authorIds = postMap.values().stream()
                .map(Post::getUserId)
                .distinct()
                .toList();
        Map<Integer, UserProjection> authorMap = userRepository.findProjectionsByIdIn(authorIds).stream()
                .collect(Collectors.toMap(UserProjection::id, Function.identity()));

        Map<Integer, PostResDTO.postListItem> cards = new LinkedHashMap<>();
        orderedIds.forEach(postId -> {
            Post post = postMap.get(postId);
            if (post == null) return;
            UserProjection user = authorMap.get(post.getUserId());
            cards.put(postId, new PostResDTO.postListItem(
                    post.getPostId(),
                    post.getTitle(),
                    post.getPostImageUrl(),
                    post.getLikeCount(),
                    post.getCommentCount(),
                    post.getViewCount(),
                    user == null ? null : new PostResDTO.userInfo(
                            user.userId(), user.nickname(), user.profileImageUrl()
                    ),
                    post.getCreatedAt()
            ));
        });
        return cards.values().stream().toList();
    }

    // 요청 파라미터 문자열("accuracy"/"latest")을 SearchSortType으로 변환, 잘못된 값이면 400
    private SearchSortType parseSortType(String value) {
        try {
            return SearchSortType.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "sort는 accuracy 또는 latest여야 합니다.");
        }
    }

    // ES가 준 마지막 히트의 정렬값(sortValues)을 JSON→Base64로 인코딩해 클라이언트에 줄 커서로 만든다
    private String encodeCursor(List<Object> sortValues) {
        try {
            byte[] json = objectMapper.writeValueAsBytes(sortValues);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(json);
        } catch (Exception exception) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "검색 커서 생성에 실패했습니다.");
        }
    }

    // 클라이언트가 보낸 커서를 Base64→JSON으로 복원해 ES searchAfter에 넘길 정렬값 목록으로 되돌린다
    private List<Object> decodeCursor(String cursor) {
        try {
            byte[] json = Base64.getUrlDecoder().decode(cursor.getBytes(StandardCharsets.UTF_8));
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception exception) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "유효하지 않은 검색 커서입니다.");
        }
    }
}
