package com.kbt.amumal.domain.post.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

public class PostReqDTO {
    @Getter
    @Setter
    public static class createPost {
        @NotBlank(message = "제목을 입력해주세요.")
        private String title;

        @NotBlank(message = "내용을 입력해주세요.")
        private String content;

        private MultipartFile postImage;
    }

    @Getter
    @Setter
    public static class updatePost {
        private String title;
        private String content;
        private MultipartFile postImage;
    }
}
