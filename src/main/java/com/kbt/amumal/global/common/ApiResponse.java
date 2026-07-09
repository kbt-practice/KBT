package com.kbt.amumal.global.common;

import lombok.Getter;

@Getter
public class ApiResponse<T> {
    private final boolean success;
    private final String message;
    private final T data;

    private ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // 성공 시
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    // 실패 시 - 구체적인 예외처리
    public static <T> ApiResponse<T> fail(String message, T data) {
        return new ApiResponse<>(false, message, data);
    }

    // 실패 시 - 유효성 검사
    public static <T> ApiResponse<T> fail(String message) {
        return new ApiResponse<>(false, message, null);
    }

    // 실패 시 - 그 외 모든 에러처리
    public static <T> ApiResponse<T> fail() {
        return new ApiResponse<>(false, "서버 에러", null);
    }
}