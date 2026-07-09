package com.kbt.amumal.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // 공통
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "유효하지 않은 요청입니다.", "잘못된 요청입니다."), // 404
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증에 실패하였습니다.", "인증이 필요합니다."), // 401
    FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 부족합니다.", "접근 권한이 없습니다."), // 403
    NOT_FOUND(HttpStatus.NOT_FOUND, "데이터가 존재하지 않습니다.", "요청한 데이터를 찾을 수 없습니다."), // 404
    CONFLICT(HttpStatus.CONFLICT, "이미 존재하는 데이터 입니다.", "데이터가 이미 존재합니다."), // 409
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 에러", "서버 내부 오류가 발생했습니다."), // 500

    // 토큰
    TOKEN_MISSING(HttpStatus.UNAUTHORIZED, "인증에 실패하였습니다.", "토큰이 없습니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "인증에 실패하였습니다.", "만료된 토큰입니다."),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "인증에 실패하였습니다.", "유효하지 않은 토큰입니다."),

    // 요청 형식
    INVALID_REQUEST_BODY(HttpStatus.BAD_REQUEST, "유효하지 않은 요청입니다.", "요청 바디가 없거나 형식이 올바르지 않습니다."),
    MISSING_HEADER(HttpStatus.BAD_REQUEST, "필수 헤더가 누락되었습니다.", "필수 헤더가 없습니다."),

    // 인증
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "인증에 실패하였습니다.", "아이디 또는 비밀번호를 확인해주세요."),

    // 유저
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 존재하는 데이터 입니다.", "중복된 이메일 입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "이미 존재하는 데이터 입니다.", "중복된 닉네임 입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "데이터가 존재하지 않습니다.", "유저 정보를 확인해주세요."),
    USER_ALREADY_WITHDRAWN(HttpStatus.CONFLICT, "이미 존재하는 데이터 입니다.", "이미 탈퇴한 유저입니다."),

    // 게시글
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "데이터가 존재하지 않습니다.", "게시글 정보를 확인해주세요."),
    POST_ALREADY_DELETED(HttpStatus.NOT_FOUND, "데이터가 존재하지 않습니다.", "이미 삭제된 게시글입니다."),
    POST_FORBIDDEN_UPDATE(HttpStatus.FORBIDDEN, "권한이 부족합니다.", "본인 게시글만 수정할 수 있습니다."),
    POST_FORBIDDEN_DELETE(HttpStatus.FORBIDDEN, "권한이 부족합니다.", "본인 게시글만 삭제할 수 있습니다."),

    // 댓글
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "데이터가 존재하지 않습니다.", "댓글 정보를 확인해주세요."),
    COMMENT_ALREADY_DELETED(HttpStatus.NOT_FOUND, "데이터가 존재하지 않습니다.", "이미 삭제된 댓글입니다."),
    COMMENT_FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 부족합니다.", "본인 댓글만 수정/삭제할 수 있습니다."),

    // 이미지
    INVALID_IMAGE_EXTENSION(HttpStatus.BAD_REQUEST, "유효하지 않은 요청입니다.", "허용되지 않는 이미지 확장자입니다. (jpg, jpeg, png, gif, webp)"),
    INVALID_IMAGE_MIME_TYPE(HttpStatus.BAD_REQUEST, "유효하지 않은 요청입니다.", "유효하지 않은 이미지 파일입니다."),
    IMAGE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "서버 에러", "이미지 업로드에 실패했습니다.");

    private final HttpStatus status;
    private final String message;
    private final String reason;
}
