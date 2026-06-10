package com.kbt.amumal.domain.post.dto;

import com.kbt.amumal.global.common.ValidationMessage;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

/**
 * 게시글 관련 요청 DTO 모음.
 * 유효성 검사 메시지는 ValidationMessage 상수를 참조한다.
 */
public class PostReqDTO {

    /** 게시글 작성 요청 */
    @Getter
    @Setter
    public static class createPost {
        @NotBlank(message = ValidationMessage.REQUIRED_POST_TITLE)
        private String title;

        @NotBlank(message = ValidationMessage.REQUIRED_POST_CONTENT)
        private String content;

        /** 선택 항목: 없으면 이미지 없이 게시글 생성 */
        private MultipartFile postImage;
    }

    /**
     * 게시글 수정 요청.
     * 모든 필드가 선택 항목이며, 전달된 값만 업데이트한다.
     */
    @Getter
    @Setter
    public static class updatePost {
        private String title;
        private String content;
        private MultipartFile postImage;
    }
}
