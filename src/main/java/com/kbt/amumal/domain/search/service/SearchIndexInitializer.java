package com.kbt.amumal.domain.search.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "search.enabled", havingValue = "true")
public class SearchIndexInitializer {

    private final PostSearchIndexGateway indexGateway;

    // 재시작할 때마다 매번 실행되는 안전장치 - 인덱스가 없으면 만든다
    @EventListener(ApplicationReadyEvent.class)
    public void ensureIndex() {
        indexGateway.ensureIndex();
    }
}
