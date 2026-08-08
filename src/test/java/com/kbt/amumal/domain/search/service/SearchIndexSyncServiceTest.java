package com.kbt.amumal.domain.search.service;

import com.kbt.amumal.domain.comment.entity.Comment;
import com.kbt.amumal.domain.comment.repository.CommentRepository;
import com.kbt.amumal.domain.post.entity.Post;
import com.kbt.amumal.domain.post.repository.PostRepository;
import com.kbt.amumal.domain.search.document.PostDocument;
import com.kbt.amumal.domain.search.entity.SearchIndexDirty;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchIndexSyncServiceTest {

    @Mock SearchDirtyBatchService dirtyBatchService;
    @Mock PostRepository postRepository;
    @Mock CommentRepository commentRepository;
    @Mock PostSearchIndexGateway indexGateway;

    @Test
    void Dirty에_있는_게시글만_문서로_재구성하고_버전으로_제거한다() {
        PostDocumentFactory factory = new PostDocumentFactory();
        SearchIndexSyncService service = new SearchIndexSyncService(
                dirtyBatchService, postRepository, commentRepository, factory, indexGateway
        );
        SearchIndexDirty target = new SearchIndexDirty(
                42, 7, LocalDateTime.now(), 0, null, null
        );
        Post post = Post.builder()
                .postId(42)
                .title("오사카 숙소")
                .content("내용")
                .userId(1)
                .build();
        Comment comment = Comment.builder()
                .commentId(1)
                .postId(42)
                .content("난바역 근처 추천")
                .build();

        when(dirtyBatchService.claimNextBatch(500)).thenReturn(List.of(target));
        when(postRepository.findAllById(List.of(42))).thenReturn(List.of(post));
        when(commentRepository.findByPostIdInAndDeletedAtIsNullOrderByPostIdAscCreatedAtAsc(List.of(42)))
                .thenReturn(List.of(comment));

        int processed = service.synchronizeNextBatch(500);

        assertThat(processed).isEqualTo(1);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<PostDocument>> documents = ArgumentCaptor.forClass(Collection.class);
        verify(indexGateway).saveAll(documents.capture());
        assertThat(documents.getValue()).singleElement()
                .extracting(PostDocument::getComments)
                .isEqualTo("난바역 근처 추천");
        verify(dirtyBatchService).complete(List.of(target));
        verify(indexGateway).deleteAll(eq(List.of()));
    }
}
