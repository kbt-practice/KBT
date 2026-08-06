package com.kbt.amumal.domain.search.service;

import com.kbt.amumal.domain.comment.entity.Comment;
import com.kbt.amumal.domain.post.entity.Post;
import com.kbt.amumal.domain.search.document.PostDocument;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PostDocumentFactoryTest {

    private final PostDocumentFactory factory = new PostDocumentFactory();

    @Test
    void 활성_댓글을_공백으로_합쳐_게시글_문서를_만든다() {
        Post post = Post.builder()
                .postId(42)
                .title("오사카 숙소 추천")
                .content("난바 근처 숙소를 찾아요")
                .userId(1)
                .build();
        List<Comment> comments = List.of(
                Comment.builder().commentId(1).postId(42).content("난바역 근처가 좋아요").build(),
                Comment.builder().commentId(2).postId(42).content("우메다도 괜찮아요").build()
        );

        PostDocument document = factory.create(post, comments);

        assertThat(document.getId()).isEqualTo("42");
        assertThat(document.getPostId()).isEqualTo(42);
        assertThat(document.getComments()).isEqualTo("난바역 근처가 좋아요 우메다도 괜찮아요");
    }
}
