package com.kbt.amumal.domain.comment.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Comment {
    private int commnetId;
    private String comment;

    private int userId;
    private int postId;
}