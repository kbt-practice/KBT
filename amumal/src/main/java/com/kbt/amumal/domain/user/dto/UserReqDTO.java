package com.kbt.amumal.domain.user.dto;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;

public class UserReqDTO {
    // 회원가입 요청 DTO
    @Getter
    public static class Signup {
        @NotBlank(message = "이메일을 입력해주세요.")
        @Email(message = "올바른 이메일 주소 형식을 입력해주세요. (예: example@example.com)")
        private String email;

        @NotBlank(message = "비밀번호를 입력해주세요.")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\W_]).{8,20}$",
                message = "비밀번호는 8자 이상, 20자 이하이며, 대문자, 소문자, 숫자, 특수문자를 각각 최소 1개 포함해야 합니다."
        )
        private String password;

        @NotBlank(message = "닉네임을 입력해주세요.")
        @Size(max = 10, message = "닉네임은 최대 10자 까지 작성 가능합니다.")
        @Pattern(regexp = "^\\S+$", message = "띄어쓰기를 없애주세요.")
        private String nickname;

        private MultipartFile profileImage;
    }

    @Getter
    public static class UpdateNickname {
        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(max = 10, message = "닉네임은 최대 10자 까지 작성 가능합니다.")
        private String nickname;
    }

    @Getter
    public static class UpdatePassword {
        @NotBlank(message = "비밀번호를 입력해주세요.")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\W_]).{8,20}$",
                message = "비밀번호는 8자 이상, 20자 이하이며, 대문자, 소문자, 숫자, 특수문자를 각각 최소 1개 포함해야 합니다."
        )
        private String password;
    }
}
