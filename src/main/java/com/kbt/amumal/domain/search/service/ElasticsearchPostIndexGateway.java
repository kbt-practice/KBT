package com.kbt.amumal.domain.search.service;

import com.kbt.amumal.domain.search.document.PostDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.query.DeleteQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

// ES 실행하는 함수
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "search.enabled", havingValue = "true")
public class ElasticsearchPostIndexGateway implements PostSearchIndexGateway {

    private final ElasticsearchOperations operations;

    // 인덱스가 없을 때 새로 만듦
    @Override
    public void ensureIndex() {
        IndexOperations indexOperations = operations.indexOps(PostDocument.class);
        if (!indexOperations.exists()) {
            indexOperations.createWithMapping();
        }
    }

    // 빈 컬렉션이면 ES에 요청을 안 보냄
    @Override
    public void saveAll(Collection<PostDocument> documents) {
        if (!documents.isEmpty()) {
            operations.save(documents);
        }
    }

    // ids 쿼리로 대상 postId들을 한 번의 요청에 묶어서 삭제
    @Override
    public void deleteAll(Collection<Integer> postIds) {
        if (postIds.isEmpty()) return;

        List<String> ids = postIds.stream().map(String::valueOf).toList();
        Query query = NativeQuery.builder()
                .withQuery(q -> q.ids(idsQuery -> idsQuery.values(ids)))
                .build();
        operations.delete(DeleteQuery.builder(query).build(), PostDocument.class);
    }
}
