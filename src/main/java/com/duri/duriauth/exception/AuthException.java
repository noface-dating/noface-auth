package com.duri.duriauth.exception;

import lombok.Getter;

/**
 * 인증/인가 도메인 예외
 *
 * <p>
 *     클라이언트 요청과 관련된 예외(토큰 오류, 인증 실패, 권한 부족 등)를 표현하며,
 *     GlobalExceptionHandler를 통해 일관된 ErrorResponse로 변환된다.
 * </p>
 */

@Getter
public class AuthException extends RuntimeException {

    private final BaseErrorCode errorCode;

    /**
     * 지정된 에러 코드로 예외를 생성합니다.
     * @param errorCode 클라이언트 에러 코드
     */
    public AuthException(BaseErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * 지정된 에러 코드와 원인 예외로 예외를 생성합니다.
     * @param errorCode 클라이언트 에러 코드
     * @param cause 원인 예외
     */
    public AuthException(BaseErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }

}
