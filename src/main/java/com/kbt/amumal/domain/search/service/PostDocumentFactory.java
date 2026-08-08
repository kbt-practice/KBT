package com.kbt.amumal.domain.search.service;

import com.kbt.amumal.domain.comment.entity.Comment;
import com.kbt.amumal.domain.post.entity.Post;
import com.kbt.amumal.domain.search.document.PostDocument;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

// 게시글 당 색인 문서 생성
@Component
public class PostDocumentFactory {

    // Post 엔티티 + 그 글의 댓글 목록을 받아 ES에 색인할 PostDocument로 변환 (댓글은 내용을 공백으로 이어붙임)
    public PostDocument create(Post post, List<Comment> comments) {
        String commentText = comments.stream()
                .map(Comment::getContent)
                .collect(Collectors.joining(" "));

        return PostDocument.builder()
                .id(post.getPostId().toString())
                .postId(post.getPostId())
                .title(post.getTitle())
                .content(post.getContent())
                .comments(commentText)
                .createdAt(post.getCreatedAt())
                .build();
    }
}
