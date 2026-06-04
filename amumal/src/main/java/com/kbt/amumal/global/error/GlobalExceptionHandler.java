package com.kbt.amumal.global.error;

import com.kbt.amumal.global.common.ApiResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;

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
        if ("Authorization".equals(e.getHeaderName())) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("인증에 실패하였습니다.", Map.of("error", "토큰이 없습니다.")));
        }
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail("필수 헤더가 누락되었습니다.", Map.of("error", e.getHeaderName() + " 헤더가 필요합니다.")));
    }

    // JWT 만료
    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ApiResponse<?>> handleExpiredJwtException(ExpiredJwtException e) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.fail("인증에 실패하였습니다.", Map.of("error", "만료된 토큰입니다.")));
    }

    // JWT 위변조
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiResponse<?>> handleJwtException(JwtException e) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.fail("인증에 실패하였습니다.", Map.of("error", "유효하지 않은 토큰입니다.")));
    }

    // 요청 바디 누락 또는 파싱 불가 (null 바디, 잘못된 JSON 등)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<?>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail("유효하지 않은 요청입니다.", Map.of("error", "요청 바디가 없거나 형식이 올바르지 않습니다.")));
    }

    // 유효성 검사 예외처리 (@RequestBody, @ModelAttribute 모두 처리)
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<?>> handleValidException(BindException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail("유효하지 않은 요청입니다.", errors));
    }

    // 500 서버 에러 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleException(Exception e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail());
    }
}