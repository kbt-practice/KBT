package com.kbt.amumal.domain.auth.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class LoginReqTest {

    private final Validator validator =
            Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("이메일 형식이 아니면 검증에 실패한다")
    void invalidEmail() {
        // given
        AuthReqDTO.LoginReq request = new AuthReqDTO.LoginReq("not-email", "Pw1234!!");

        // when
        Set<ConstraintViolation<AuthReqDTO.LoginReq>> violations =
                validator.validate(request);

        // then
        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("email");
    }
}
