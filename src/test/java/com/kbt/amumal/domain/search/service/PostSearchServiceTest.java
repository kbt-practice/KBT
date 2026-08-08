package com.kbt.amumal.domain.search.service;

import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import com.kbt.amumal.domain.post.repository.PostRepository;
import com.kbt.amumal.domain.search.document.PostDocument;
import com.kbt.amumal.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.test.util.ReflectionTestUtils;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostSearchServiceTest {

    @Mock ElasticsearchOperations operations;
    @Mock PostRepository postRepository;
    @Mock UserRepository userRepository;
    @Mock SearchHits<PostDocument> searchHits;

    private PostSearchService service;

    @BeforeEach
    void setUp() {
        service = new PostSearchService(operations, postRepository, userRepository, new ObjectMapper());
        ReflectionTestUtils.setField(service, "titleBoost", 4.0f);
        ReflectionTestUtils.setField(service, "contentBoost", 2.0f);
        ReflectionTestUtils.setField(service, "commentsBoost", 1.0f);
        ReflectionTestUtils.setField(service, "fuzziness", "AUTO");
        ReflectionTestUtils.setField(service, "maxExpansions", 25);
        ReflectionTestUtils.setField(service, "prefixLength", 0);
        when(searchHits.getSearchHits()).thenReturn(java.util.List.of());
    }

    @Test
    void 정확도순은_AUTO_fuzzy와_세_개의_정렬_기준을_사용한다() {
        when(operations.search(org.mockito.ArgumentMatchers.any(NativeQuery.class), eq(PostDocument.class)))
                .thenReturn(searchHits);

        service.search("오사카 숙소", "accuracy", null, 10);

        ArgumentCaptor<NativeQuery> queryCaptor = ArgumentCaptor.forClass(NativeQuery.class);
        verify(operations).search(queryCaptor.capture(), eq(PostDocument.class));
        NativeQuery query = queryCaptor.getValue();

        assertThat(query.getQuery().multiMatch().fuzziness()).isEqualTo("AUTO");
        assertThat(query.getQuery().multiMatch().type()).isEqualTo(TextQueryType.BestFields);
        assertThat(query.getQuery().multiMatch().fields())
                .containsExactly("title^4.0", "content^2.0", "comments^1.0");
        assertThat(query.getSortOptions()).hasSize(3);
    }

    @Test
    void 최신순은_생성일과_게시글_ID로_정렬한다() {
        when(operations.search(org.mockito.ArgumentMatchers.any(NativeQuery.class), eq(PostDocument.class)))
                .thenReturn(searchHits);

        service.search("오사카", "latest", null, 10);

        ArgumentCaptor<NativeQuery> queryCaptor = ArgumentCaptor.forClass(NativeQuery.class);
        verify(operations).search(queryCaptor.capture(), eq(PostDocument.class));
        assertThat(queryCaptor.getValue().getSortOptions()).hasSize(2);
    }
}
