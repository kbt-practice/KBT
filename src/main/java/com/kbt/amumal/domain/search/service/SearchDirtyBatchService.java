package com.kbt.amumal.domain.search.service;

import com.kbt.amumal.domain.search.entity.SearchIndexDirty;
import com.kbt.amumal.domain.search.repository.SearchIndexDirtyRepository;
import com.kbt.amumal.domain.search.repository.SearchIndexDirtyRepositoryCustom.FailureRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "search.enabled", havingValue = "true")
public class SearchDirtyBatchService {

    private static final int CLAIM_MINUTES = 10;

    private final SearchIndexDirtyRepository dirtyRepository;

    /**
     * 다른 파드가 같은 항목을 처리하지 않도록 짧은 트랜잭션으로 대상을 선점한다.
     * claimNextBatch/complete/recordFailure를 각각 REQUIRES_NEW로 분리한 이유:
     * 이 셋을 하나의 긴 트랜잭션으로 묶으면, ES 색인(SearchIndexSyncService, DB 트랜잭션 밖의 작업)이
     * 끝날 때까지 select ... for update 락(findProcessable)을 계속 붙들고 있게 되어 다른 파드가 오래 대기한다.
     * 선점은 선점 시점에 바로 커밋해서 락을 짧게 끝내고, 완료/실패 기록도 각각 독립적으로 커밋되게 한다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<SearchIndexDirty> claimNextBatch(int batchSize) {
        LocalDateTime now = LocalDateTime.now();
        List<SearchIndexDirty> targets = dirtyRepository.findProcessable(
                now,
                PageRequest.of(0, batchSize)
        );
        if (!targets.isEmpty()) {
            List<Integer> postIds = targets.stream().map(SearchIndexDirty::getPostId).toList();
            dirtyRepository.markClaimed(postIds, now.plusMinutes(CLAIM_MINUTES));
        }
        return targets;
    }

    // 색인 반영이 끝난 대상들을 dirty에서 제거
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void complete(List<SearchIndexDirty> targets) {
        dirtyRepository.deleteAllIfVersionMatches(targets);
    }

    // 배치 전체가 같은 예외로 실패했을 때, 대상들의 실패를 한 번에 기록하고 다음 시도 시각을 지수 백오프로 예약한다.
    // 1회차 60s, 2회차 120s, 3회차 240s ... 6회차부터는 3600s(1시간)로 고정. row마다 자기 retryCount 기준으로
    // 각자 다른 nextRetryAt을 계산해야 해서, 계산은 여기서 하고 실제 UPDATE는 JDBC batch로 한 번에 보낸다.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(List<SearchIndexDirty> targets, RuntimeException exception) {
        if (targets.isEmpty()) return;

        String rawMessage = exception.getMessage() == null
                ? exception.getClass().getSimpleName()
                : exception.getMessage();
        String message = rawMessage.length() > 1000 ? rawMessage.substring(0, 1000) : rawMessage;

        LocalDateTime now = LocalDateTime.now();
        List<FailureRecord> failures = targets.stream()
                .map(target -> {
                    long delaySeconds = Math.min(3600L, 60L << Math.min(target.getRetryCount(), 5));
                    return new FailureRecord(
                            target.getPostId(), target.getVersion(), now.plusSeconds(delaySeconds), message
                    );
                })
                .toList();

        dirtyRepository.recordFailures(failures);
    }
}
