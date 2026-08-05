package com.kbt.amumal.domain.search.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Mapping;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.time.LocalDateTime;

// 제목·내용·댓글 내용을 하나의 문서로 색인한다 (유저 이름 등은 검색 범위에서 제외)
@Document(indexName = "posts")
@Setting(settingPath = "elasticsearch/post-settings.json")
@Mapping(mappingPath = "elasticsearch/post-mappings.json")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostDocument {

    @Id
    private String id; // postId를 문자열로 변환한 값 (ES 문서 ID)

    private Integer postId;

    private String title;

    private String content;

    // 게시글에 달린 모든 댓글 내용을 공백으로 이어붙인 값 (댓글 CUD 시마다 갱신)
    private String comments;

    private LocalDateTime createdAt;
}
