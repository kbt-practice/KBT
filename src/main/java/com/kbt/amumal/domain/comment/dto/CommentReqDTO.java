package com.kbt.amumal.domain.comment.dto;

import com.kbt.amumal.global.common.ValidationMessage;
import jakarta.validation.constraints.NotBlank;

public class CommentReqDTO {

    public record createComment(
            @NotBlank(message = ValidationMessage.REQUIRED_COMMENT_CONTENT)
            String content
    ) {}

    public record updateComment(
            @NotBlank(message = ValidationMessage.REQUIRED_COMMENT_CONTENT)
            String content
    ) {}
}
