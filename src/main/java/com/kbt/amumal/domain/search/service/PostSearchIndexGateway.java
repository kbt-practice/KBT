package com.kbt.amumal.domain.search.service;

import com.kbt.amumal.domain.search.document.PostDocument;

import java.util.Collection;

// ES 색인 실행 인터페이스
public interface PostSearchIndexGateway {
    // 인덱스가 없으면 매핑/세팅까지 포함해서 새로 만든다
    void ensureIndex();
    // 문서들을 upsert (있으면 갱신, 없으면 생성)
    void saveAll(Collection<PostDocument> documents);
    // 주어진 postId들의 문서를 인덱스에서 삭제
    void deleteAll(Collection<Integer> postIds);
}
