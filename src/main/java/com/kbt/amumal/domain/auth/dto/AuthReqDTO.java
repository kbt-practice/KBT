package com.kbt.amumal.domain.auth.dto;

import com.kbt.amumal.global.common.ValidationMessage;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class AuthReqDTO {

    public record LoginReq(
            @NotBlank(message = ValidationMessage.REQUIRED_EMAIL)
            @Email(message = ValidationMessage.INVALID_EMAIL_FORMAT)
            String email,

            @NotBlank(message = ValidationMessage.REQUIRED_PASSWORD)
            @Pattern(
                    regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\W_]).{8,20}$",
                    message = ValidationMessage.INVALID_PASSWORD_FORMAT
            )
            String password
    ) {}
}
