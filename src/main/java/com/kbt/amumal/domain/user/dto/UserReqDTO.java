package com.kbt.amumal.domain.user.dto;

import com.kbt.amumal.global.common.ValidationMessage;
import jakarta.validation.constraints.*;

public class UserReqDTO {

    public record Signup(
            @NotBlank(message = ValidationMessage.REQUIRED_EMAIL)
            @Email(message = ValidationMessage.INVALID_EMAIL_FORMAT)
            String email,

            @NotBlank(message = ValidationMessage.REQUIRED_PASSWORD)
            @Pattern(
                    regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\W_]).{8,20}$",
                    message = ValidationMessage.INVALID_PASSWORD_FORMAT
            )
            String password,

            @NotBlank(message = ValidationMessage.REQUIRED_NICKNAME)
            @Size(max = 10, message = ValidationMessage.NICKNAME_MAX_LENGTH)
            @Pattern(regexp = "^\\S+$", message = ValidationMessage.NICKNAME_NO_SPACE)
            String nickname
    ) {}

    public record UpdateNickname(
            @NotBlank(message = ValidationMessage.NICKNAME_REQUIRED_UPDATE)
            @Size(max = 10, message = ValidationMessage.NICKNAME_MAX_LENGTH)
            String nickname
    ) {}

    public record UpdatePassword(
            @NotBlank(message = ValidationMessage.REQUIRED_PASSWORD)
            @Pattern(
                    regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\W_]).{8,20}$",
                    message = ValidationMessage.INVALID_PASSWORD_FORMAT
            )
            String password
    ) {}
}
