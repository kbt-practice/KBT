package com.kbt.amumal.domain.post.dto;

import com.kbt.amumal.global.common.ValidationMessage;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.multipart.MultipartFile;

public class PostReqDTO {

    public record createPost(
            @NotBlank(message = ValidationMessage.REQUIRED_POST_TITLE)
            String title,

            @NotBlank(message = ValidationMessage.REQUIRED_POST_CONTENT)
            String content,

            MultipartFile postImage
    ) {}

    public record updatePost(
            String title,
            String content,
            MultipartFile postImage
    ) {}
}
