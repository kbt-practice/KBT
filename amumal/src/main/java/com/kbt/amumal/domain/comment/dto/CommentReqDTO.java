package com.kbt.amumal.domain.comment.dto;

import com.kbt.amumal.global.common.ValidationMessage;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 댓글 관련 요청 DTO 모음.
 * 유효성 검사 메시지는 ValidationMessage 상수를 참조한다.
 */
public class CommentReqDTO {

    /** 댓글 작성 요청 */
    @Getter
    @Setter
    public static class createComment {
        @NotBlank(message = ValidationMessage.REQUIRED_COMMENT_CONTENT)
        private String content;
    }

    /** 댓글 수정 요청 */
    @Getter
    @Setter
    public static class updateComment {
        @NotBlank(message = ValidationMessage.REQUIRED_COMMENT_CONTENT)
        private String content;
    }
}
