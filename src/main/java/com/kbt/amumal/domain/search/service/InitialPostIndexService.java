package com.kbt.amumal.domain.search.service;

import com.kbt.amumal.domain.comment.entity.Comment;
import com.kbt.amumal.domain.comment.repository.CommentRepository;
import com.kbt.amumal.domain.post.entity.Post;
import com.kbt.amumal.domain.post.repository.PostRepository;
import com.kbt.amumal.domain.search.document.PostDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// 기존 게시글 색인
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "search.initial-index.enabled", havingValue = "true")
public class InitialPostIndexService {

    private static final int BATCH_SIZE = 500;

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostDocumentFactory documentFactory;
    private final PostSearchIndexGateway indexGateway;

    // 앱 기동 시(search.initial-index.enabled=true일 때만) MySQL의 게시글 전체를 커서로 훑어 ES에 일괄 색인
    @EventListener(ApplicationReadyEvent.class)
    public void indexExistingPosts() {
        indexGateway.ensureIndex();
        Integer cursor = 0;
        int indexedCount = 0;

        while (true) {
            List<Post> posts = postRepository.findPostsWithCursor(cursor, BATCH_SIZE);
            if (posts.isEmpty()) break;

            List<Integer> postIds = posts.stream().map(Post::getPostId).toList();
            Map<Integer, List<Comment>> commentsByPostId = commentRepository
                    .findByPostIdInAndDeletedAtIsNullOrderByPostIdAscCreatedAtAsc(postIds)
                    .stream()
                    .collect(Collectors.groupingBy(Comment::getPostId));

            List<PostDocument> documents = posts.stream()
                    .map(post -> documentFactory.create(
                            post,
                            commentsByPostId.getOrDefault(post.getPostId(), List.of())
                    ))
                    .toList();
            indexGateway.saveAll(documents);

            indexedCount += documents.size();
            cursor = posts.get(posts.size() - 1).getPostId();
        }

        log.info("기존 게시글 전체 색인 완료 - {}개", indexedCount);
    }
}
