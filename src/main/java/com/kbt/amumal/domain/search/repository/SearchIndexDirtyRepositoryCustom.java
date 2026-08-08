package com.kbt.amumal.domain.search.repository;

import com.kbt.amumal.domain.search.entity.SearchIndexDirty;

import java.time.LocalDateTime;
import java.util.List;

public interface SearchIndexDirtyRepositoryCustom {
    // complete()가 넘긴 대상들을 (postId, version) 쌍으로 한 번에 지운다.
    // 이 시점엔 claim 때의 락이 이미 풀려 있어 row마다 version이 다를 수 있어서, 단순 postId IN으로는 못 묶고
    // JDBC batch(addBatch/executeBatch)로 건별 DELETE를 한 번의 왕복에 모아 보낸다.
    void deleteAllIfVersionMatches(List<SearchIndexDirty> targets);

    // recordFailure를 배치로 묶기 위한 캐리어. row마다 nextRetryAt(자기 retryCount 기준 백오프 계산 결과)이
    // 달라서 SearchIndexDirty 엔티티만으로는 못 묶고, 호출부(SearchDirtyBatchService)가 계산해서 넘겨준다.
    record FailureRecord(Integer postId, long version, LocalDateTime nextRetryAt, String message) {
    }

    // 여러 실패 대상을 (postId, version)별로 한 번에 기록 - JDBC batch로 건별 왕복 대신 한 번에 전송
    void recordFailures(List<FailureRecord> failures);
}
