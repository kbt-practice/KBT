package com.kbt.amumal.domain.search.service;

import com.kbt.amumal.domain.search.repository.SearchIndexDirtyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchDirtyService {

    private final SearchIndexDirtyRepository searchIndexDirtyRepository;

    // 게시글·댓글 변경 트랜잭션 안에서 호출해 색인 대상을 유실 없이 기록
    public void markDirty(Integer postId) {
        searchIndexDirtyRepository.markDirty(postId);
    }
}
