package com.kbt.amumal.domain.comment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

public class CommentReqDTO {
    @Getter
    @Setter
    public static class createComment {
        @NotBlank(message = "댓글 내용을 입력해주세요.")
        private String content;
    }

    @Getter
    @Setter
    public static class updateComment {
        @NotBlank(message = "댓글 내용을 입력해주세요.")
        private String content;
    }
}