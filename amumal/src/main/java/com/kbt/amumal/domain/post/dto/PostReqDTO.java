package com.kbt.amumal.domain.post.dto;

import com.kbt.amumal.global.common.ValidationMessage;
import jakarta.validation.constraints.NotBlank;

public class PostReqDTO {

    public record createPost(
            @NotBlank(message = ValidationMessage.REQUIRED_POST_TITLE)
            String title,

            @NotBlank(message = ValidationMessage.REQUIRED_POST_CONTENT)
            String content
    ) {}

    public record updatePost(String title, String content) {}
}
