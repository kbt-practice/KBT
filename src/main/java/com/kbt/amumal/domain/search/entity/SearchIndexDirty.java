package com.kbt.amumal.domain.search.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * MySQL 원본 데이터보다 검색 문서가 오래된 게시글을 기록
 * postId를 PK로 두어 같은 게시글의 연속 변경을 하나의 재색인 작업 처리
 */
@Entity
@Table(
        name = "search_index_dirty",
        indexes = {
                @Index(name = "idx_search_dirty_requested", columnList = "requested_at"),
                @Index(name = "idx_search_dirty_retry", columnList = "next_retry_at, requested_at")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class SearchIndexDirty {

    @Id
    @Column(name = "post_id", nullable = false)
    private Integer postId;

    // 배치 중 변경사항이 작동하면 version이 올라가는데, 기존 변경사항이 완료되고 삭제될 때 삭제를 무시하게 함 → 다음 배치에 반영됨
    @Column(nullable = false)
    private long version;

    // 오래 기다린것부터 처리하기 위한 생성 시각
    @Column(name = "requested_at", nullable = false, columnDefinition = "TIMESTAMP(6)")
    private LocalDateTime requestedAt;

    // 실패할 때마다 +1, 지수 백오프 계산에 사용
    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    // ① 실패 후 재시도까지 기다리는 시각
    // ② claim 시 "지금부터 10분간 내가 처리 중"이라는 리스 만료 시각.
    @Column(name = "next_retry_at", columnDefinition = "TIMESTAMP(6)")
    private LocalDateTime nextRetryAt;

    // 마지막 실패 원인 (운영 디버깅용, 컬럼 길이에 맞춰 1000자로 자름)
    @Column(name = "last_error", length = 1000)
    private String lastError;
}
