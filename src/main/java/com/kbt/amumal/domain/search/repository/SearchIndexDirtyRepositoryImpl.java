package com.kbt.amumal.domain.search.repository;

import com.kbt.amumal.domain.search.entity.SearchIndexDirty;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class SearchIndexDirtyRepositoryImpl implements SearchIndexDirtyRepositoryCustom {

    private static final String DELETE_SQL =
            "DELETE FROM search_index_dirty WHERE post_id = ? AND version = ?";

    private static final String RECORD_FAILURE_SQL = """
            UPDATE search_index_dirty
            SET retry_count = retry_count + 1, next_retry_at = ?, last_error = ?
            WHERE post_id = ? AND version = ?
            """;

    // JpaTransactionManager가 관리하는 것과 같은 DataSource라서, 이 배치도 호출부의 트랜잭션에 그대로 참여한다.
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void deleteAllIfVersionMatches(List<SearchIndexDirty> targets) {
        if (targets.isEmpty()) return;

        jdbcTemplate.batchUpdate(DELETE_SQL, targets, targets.size(), (ps, target) -> {
            ps.setInt(1, target.getPostId());
            ps.setLong(2, target.getVersion());
        });
    }

    @Override
    public void recordFailures(List<FailureRecord> failures) {
        if (failures.isEmpty()) return;

        jdbcTemplate.batchUpdate(RECORD_FAILURE_SQL, failures, failures.size(), (ps, failure) -> {
            ps.setObject(1, failure.nextRetryAt());
            ps.setString(2, failure.message());
            ps.setInt(3, failure.postId());
            ps.setLong(4, failure.version());
        });
    }
}
