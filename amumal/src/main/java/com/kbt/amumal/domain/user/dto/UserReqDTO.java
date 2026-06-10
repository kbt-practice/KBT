package com.kbt.amumal.domain.user.dto;

import com.kbt.amumal.global.common.ValidationMessage;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 유저 관련 요청 DTO 모음.
 * 유효성 검사 메시지는 ValidationMessage 상수를 참조한다.
 */
public class UserReqDTO {

    /** 회원가입 요청 */
    @Getter
    @Setter
    public static class Signup {
        @NotBlank(message = ValidationMessage.REQUIRED_EMAIL)
        @Email(message = ValidationMessage.INVALID_EMAIL_FORMAT)
        private String email;

        /** 정규식: 8~20자, 대문자·소문자·숫자·특수문자 각 1개 이상 */
        @NotBlank(message = ValidationMessage.REQUIRED_PASSWORD)
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\W_]).{8,20}$",
                message = ValidationMessage.INVALID_PASSWORD_FORMAT
        )
        private String password;

        /** 공백 포함 불가, 최대 10자 */
        @NotBlank(message = ValidationMessage.REQUIRED_NICKNAME)
        @Size(max = 10, message = ValidationMessage.NICKNAME_MAX_LENGTH)
        @Pattern(regexp = "^\\S+$", message = ValidationMessage.NICKNAME_NO_SPACE)
        private String nickname;

        /** 선택 항목: 없으면 프로필 이미지 없이 가입 */
        private MultipartFile profileImage;
    }

    /** 닉네임 수정 요청 */
    @Getter
    public static class UpdateNickname {
        @NotBlank(message = ValidationMessage.NICKNAME_REQUIRED_UPDATE)
        @Size(max = 10, message = ValidationMessage.NICKNAME_MAX_LENGTH)
        private String nickname;
    }

    /** 비밀번호 수정 요청 */
    @Getter
    public static class UpdatePassword {
        /** 정규식: 8~20자, 대문자·소문자·숫자·특수문자 각 1개 이상 */
        @NotBlank(message = ValidationMessage.REQUIRED_PASSWORD)
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\W_]).{8,20}$",
                message = ValidationMessage.INVALID_PASSWORD_FORMAT
        )
        private String password;
    }

    /** 프로필 이미지 수정 요청 */
    @Getter
    public static class updateProfile {
        private MultipartFile profileImage;
    }
}
