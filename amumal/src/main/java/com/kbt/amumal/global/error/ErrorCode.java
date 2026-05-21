package com.kbt.amumal.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "유효하지 않은 요청입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증에 실패하였습니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 부족합니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "데이터가 존재하지 않습니다."),
    CONFLICT(HttpStatus.CONFLICT, "이미 존재하는 데이터 입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 에러");

    private final HttpStatus status;
    private final String message;
}