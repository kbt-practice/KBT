package com.kbt.amumal.domain.search.repository;

import com.kbt.amumal.domain.search.entity.SearchIndexDirty;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import jakarta.persistence.LockModeType;

public interface SearchIndexDirtyRepository extends JpaRepository<SearchIndexDirty, Integer>, SearchIndexDirtyRepositoryCustom {

    // postId가 PK라 같은 글이 연달아 바뀌어도 row가 하나로 합쳐진다 (UPSERT).
    // 이미 대기 중인 row가 있으면 version만 +1 하고 재시도 상태(retry_count/next_retry_at/last_error)를
    // 전부 초기화한다 — "재시도 대기 중이던 실패한 색인"도 새 변경이 오면 즉시 다시 처리 대상이 되도록.
    @Modifying(flushAutomatically = true)
    @Query(value = """
            INSERT INTO search_index_dirty
                (post_id, version, requested_at, retry_count, next_retry_at, last_error)
            VALUES
                (:postId, 1, CURRENT_TIMESTAMP(6), 0, NULL, NULL)
            ON DUPLICATE KEY UPDATE
                version = version + 1,
                requested_at = CURRENT_TIMESTAMP(6),
                retry_count = 0,
                next_retry_at = NULL,
                last_error = NULL
            """, nativeQuery = true)
    void markDirty(@Param("postId") Integer postId);

    // next_retry_at이 비어있거나(=한 번도 안 건드림) 지났으면(=재시도 시간 됐거나 리스 만료됨) 대상.
    // 비관적 락으로 잠가서, 같은 순간에 여러 파드가 findProcessable을 돌려도 서로 다른 row를 집어가게 한다.
    @Query("""
            SELECT d FROM SearchIndexDirty d
            WHERE d.nextRetryAt IS NULL OR d.nextRetryAt <= :now
            ORDER BY d.requestedAt ASC
            """)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<SearchIndexDirty> findProcessable(@Param("now") LocalDateTime now, Pageable pageable);

    // next_retry_at을 "지금부터 N분 뒤"로 밀어서, 그 시간 동안은 findProcessable에 다시 안 걸리게 선점(리스)한다.
    // 여기선 version 조건이 필요 없다 - findProcessable의 PESSIMISTIC_WRITE 락을 같은 트랜잭션에서 그대로 들고
    // 있는 상태라, 이 postId들의 row는 이 트랜잭션이 끝나기 전까지 다른 트랜잭션이 절대 못 바꾼다.
    // 그래서 postId만으로 안전하게 한 번의 UPDATE로 묶을 수 있다 (건별 왕복 대신).
    @Modifying
    @Query("""
            UPDATE SearchIndexDirty d
            SET d.nextRetryAt = :claimedUntil
            WHERE d.postId IN :postIds
            """)
    int markClaimed(
            @Param("postIds") List<Integer> postIds,
            @Param("claimedUntil") LocalDateTime claimedUntil
    );

}
