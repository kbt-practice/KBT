package com.kbt.amumal.domain.search.service;

import com.kbt.amumal.domain.comment.entity.Comment;
import com.kbt.amumal.domain.comment.repository.commentRepository;
import com.kbt.amumal.domain.post.entity.Post;
import com.kbt.amumal.domain.post.repository.PostRepository;
import com.kbt.amumal.domain.search.document.PostDocument;
import com.kbt.amumal.domain.search.entity.SearchIndexDirty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "search.enabled", havingValue = "true")
public class SearchIndexSyncService {

    private final SearchDirtyBatchService dirtyBatchService;
    private final PostRepository postRepository;
    private final commentRepository commentRepository;
    private final PostDocumentFactory documentFactory;
    private final PostSearchIndexGateway indexGateway;

    @Value("${search.sync.batch-size:500}")
    private int configuredBatchSize;

    // 설정된 주기마다(기본 5분) 배치 크기만큼 dirty 큐를 처리하는 스케줄 진입점
    @Scheduled(fixedDelayString = "${search.sync.fixed-delay-ms:300000}")
    public void synchronizeDirtyPosts() {
        synchronizeNextBatch(configuredBatchSize);
    }

    // dirty 큐에서 한 배치를 선점해 MySQL 최신 상태로 ES를 갱신/삭제하고, 처리한 건수를 반환
    public int synchronizeNextBatch(int batchSize) {
        // 1. dirty 큐에서 배치를 선점 (SearchDirtyBatchService.claimNextBatch 참고 - 리스 방식)
        List<SearchIndexDirty> targets = dirtyBatchService.claimNextBatch(batchSize);
        if (targets.isEmpty()) return 0;

        // 2. 대상 postId들의 최신 MySQL 상태를 한 번에 조회 (postId당 쿼리 N번이 아니라 IN 절 1번)
        List<Integer> postIds = targets.stream().map(SearchIndexDirty::getPostId).toList();
        Map<Integer, Post> postMap = postRepository.findAllById(postIds).stream()
                .collect(Collectors.toMap(Post::getPostId, Function.identity()));

        // 삭제된(소프트 딜리트) 글의 댓글까지 굳이 조회할 필요 없으니, 살아있는 글만 골라서 댓글을 불러온다
        List<Integer> activePostIds = postMap.values().stream()
                .filter(post -> post.getDeletedAt() == null)
                .map(Post::getPostId)
                .toList();
        Map<Integer, List<Comment>> commentsByPostId = loadComments(activePostIds);

        // 3. dirty였던 이유가 "삭제 처리"일 수도 "삭제됨"일 수도 있어서 분기한다.
        //    postMap에 없거나(완전 삭제) deletedAt이 찍혀 있으면(소프트 딜리트) → ES 문서를 지워야 할 대상
        //    나머지는 → 최신 내용으로 다시 색인해야 할 대상
        List<PostDocument> documents = new ArrayList<>();
        List<Integer> deletedPostIds = new ArrayList<>();
        for (Integer postId : postIds) {
            Post post = postMap.get(postId);
            if (post == null || post.getDeletedAt() != null) {
                deletedPostIds.add(postId);
            } else {
                documents.add(documentFactory.create(
                        post,
                        commentsByPostId.getOrDefault(postId, List.of())
                ));
            }
        }

        // 4. ES 반영 성공 시에만 dirty 큐에서 지운다(complete). 하나라도 실패하면 배치 전체를
        //    실패로 간주해 재시도 예약한다 (부분 성공을 가려낼 수 없는 saveAll/deleteAll 특성상,
        //    이미 반영된 것도 한 번 더 재시도되는 게 낫다는 판단 - upsert라 중복 반영은 안전함)
        try {
            indexGateway.saveAll(documents);
            indexGateway.deleteAll(deletedPostIds);
            dirtyBatchService.complete(targets);
            log.info("검색 증분 색인 완료 - 갱신 {}개, 삭제 {}개", documents.size(), deletedPostIds.size());
        } catch (RuntimeException exception) {
            dirtyBatchService.recordFailure(targets, exception);
            log.error("검색 증분 색인 실패 - 대상 {}개", targets.size(), exception);
        }

        return targets.size();
    }

    // 주어진 postId들의 살아있는 댓글을 postId별로 묶어서 반환 (빈 입력이면 조회 스킵)
    private Map<Integer, List<Comment>> loadComments(Collection<Integer> postIds) {
        if (postIds.isEmpty()) return Map.of();
        return commentRepository
                .findByPostIdInAndDeletedAtIsNullOrderByPostIdAscCreatedAtAsc(List.copyOf(postIds))
                .stream()
                .collect(Collectors.groupingBy(Comment::getPostId));
    }

}
