package com.kbt.amumal.global.error;

import com.kbt.amumal.global.common.ApiResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // 일반적인 예외
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<?>> handleCustomException(CustomException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.fail(errorCode.getMessage(), Map.of("error", e.getReason())));
    }

    // Authorization 헤더 누락
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiResponse<?>> handleMissingRequestHeaderException(MissingRequestHeaderException e) {
        ErrorCode errorCode = "Authorization".equals(e.getHeaderName())
                ? ErrorCode.TOKEN_MISSING
                : ErrorCode.MISSING_HEADER;
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.fail(errorCode.getMessage(), Map.of("error", errorCode.getReason())));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<?>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        ErrorCode errorCode = ErrorCode.INVALID_REQUEST_BODY;
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.fail(errorCode.getMessage(), Map.of("error", errorCode.getReason())));
    }

    // JWT 만료
    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ApiResponse<?>> handleExpiredJwtException(ExpiredJwtException e) {
        ErrorCode errorCode = ErrorCode.TOKEN_EXPIRED;
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.fail(errorCode.getMessage(), Map.of("error", errorCode.getReason())));
    }

    // JWT 위변조
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiResponse<?>> handleJwtException(JwtException e) {
        ErrorCode errorCode = ErrorCode.TOKEN_INVALID;
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.fail(errorCode.getMessage(), Map.of("error", errorCode.getReason())));
    }

    // 유효성 검사 예외처리 (@RequestBody, @ModelAttribute 모두 처리)
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<?>> handleValidException(BindException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity
                .status(ErrorCode.BAD_REQUEST.getStatus())
                .body(ApiResponse.fail(ErrorCode.BAD_REQUEST.getMessage(), errors));
    }

    // 500 서버 에러 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleException(Exception e) {
        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                .body(ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR.getMessage(), Map.of("error", ErrorCode.INTERNAL_SERVER_ERROR.getReason())));
    }
}
