package com.duri.duriauth.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 인증/인가 도메인에서 사용하는 에러 코드 정의
 *
 * <p>
 *     JWT 검증, 로그인 인증, 권한 검증 등 인증 관련 예외 상황을 HTTP Status Code와 함께 관리한다.
 * </p>
 */

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements BaseErrorCode {

    // 401 Unauthorized : Token
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH-401-1", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH-401-2", "토큰이 만료되었습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH-401-3", "유효하지 않은 리프레쉬 토큰입니다."),

    // 401 Unauthorized : Credentials (로그인)
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH-401-4", "아이디 또는 비밀번호가 올바르지 않습니다."),

    // 403 Forbidden
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "AUTH-403-1", "접근 권한이 없습니다."),
    DORMANT_ACCOUNT(HttpStatus.FORBIDDEN, "AUTH-403-2", "휴면 계정입니다."),

    // 404 Not Found : 조회 API
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH-404-1", "회원을 찾을 수 없습니다."),

    // 500 Internal Server Error
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "AUTH-500-1", "서버 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

}
