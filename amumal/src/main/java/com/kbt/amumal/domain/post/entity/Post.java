package com.kbt.amumal.domain.post.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Post {
    private String postId;
    private String content;
    private String postImageUrl;

    private String user_id;
}