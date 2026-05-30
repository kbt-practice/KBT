package com.kbt.amumal.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

public class AuthReqDTO {
    // 로그인 요청 DTO
    @Getter
    public static class LoginReq {
        @NotBlank(message = "이메일을 입력해주세요.")
        @Email(message = "올바른 이메일 주소 형식을 입력해주세요. (예: example@adapterz.kr)")
        private String email;

        @NotBlank(message = "비밀번호를 입력해주세요.")
        private String password;
    }
}
