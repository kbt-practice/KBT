package com.kbt.amumal.global.error;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
    private final ErrorCode errorCode;
    private final String reason;

    // 상세 reason 직접 지정
    public CustomException(ErrorCode errorCode, String reason) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.reason = reason;
    }

    // ErrorCode의 기본 reason 사용
    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.reason = errorCode.getReason();
    }
}
