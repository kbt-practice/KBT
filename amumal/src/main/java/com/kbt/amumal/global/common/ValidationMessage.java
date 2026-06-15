package com.kbt.amumal.global.common;

/**
 * Jakarta Validation 애노테이션(@NotBlank, @Pattern 등)의 message 속성에 사용하는 메시지 상수 모음.
 *
 * 애노테이션의 message 속성은 컴파일 타임 상수(static final)만 허용하므로
 * 이 클래스의 상수를 직접 참조한다.
 * 메시지를 수정할 때는 이 파일 한 곳만 변경하면 모든 DTO에 일괄 반영된다.
 */
public final class ValidationMessage {

    private ValidationMessage() {}

    // ===================== 이메일 =====================

    /** 이메일 필드가 비어 있을 때 */
    public static final String REQUIRED_EMAIL = "이메일을 입력해주세요.";

    /** 이메일 형식이 올바르지 않을 때 */
    public static final String INVALID_EMAIL_FORMAT = "올바른 이메일 주소 형식을 입력해주세요. (예: example@example.com)";

    // ===================== 비밀번호 =====================

    /** 비밀번호 필드가 비어 있을 때 */
    public static final String REQUIRED_PASSWORD = "비밀번호를 입력해주세요.";

    /**
     * 비밀번호 정규식 조건 미충족 시.
     * 조건: 8~20자, 대문자·소문자·숫자·특수문자 각 1개 이상
     */
    public static final String INVALID_PASSWORD_FORMAT =
            "비밀번호는 8자 이상, 20자 이하이며, 대문자, 소문자, 숫자, 특수문자를 각각 최소 1개 포함해야 합니다.";

    // ===================== 닉네임 =====================

    /** 닉네임 필드가 비어 있을 때 (회원가입) */
    public static final String REQUIRED_NICKNAME = "닉네임을 입력해주세요.";

    /** 닉네임 필드가 비어 있을 때 (닉네임 수정) */
    public static final String NICKNAME_REQUIRED_UPDATE = "닉네임은 필수입니다.";

    /** 닉네임이 최대 길이(10자)를 초과할 때 */
    public static final String NICKNAME_MAX_LENGTH = "닉네임은 최대 10자 까지 작성 가능합니다.";

    /** 닉네임에 공백이 포함될 때 */
    public static final String NICKNAME_NO_SPACE = "띄어쓰기를 없애주세요.";

    // ===================== 게시글 =====================

    /** 게시글 제목이 비어 있을 때 */
    public static final String REQUIRED_POST_TITLE = "제목을 입력해주세요.";

    /** 게시글 내용이 비어 있을 때 */
    public static final String REQUIRED_POST_CONTENT = "내용을 입력해주세요.";

    // ===================== 댓글 =====================

    /** 댓글 내용이 비어 있을 때 */
    public static final String REQUIRED_COMMENT_CONTENT = "댓글 내용을 입력해주세요.";
}
