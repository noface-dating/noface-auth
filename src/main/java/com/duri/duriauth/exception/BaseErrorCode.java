package com.duri.duriauth.exception;

import java.io.Serializable;
import org.springframework.http.HttpStatus;

/**
 * 공통 에러 코드 인터페이스
 *
 * <p>
 *     모든 ErrorCode는 이 인터페이스를 구현하며, 클라이언트 응답 및 예외 처리 시 사용된다.
 * </p>
 */

public interface BaseErrorCode extends Serializable {

    /**
     * 해당 에러와 매핑되는 HTTP 상태 코드
     * 예: UNAUTHORIZED -> 401, INTERNAL_SERVER_ERROR -> 500
     *
     * @return HTTP Status Code
     */
    HttpStatus getStatus();

    /**
     * 시스템 내에서 고유하게 관리되는 에러 코드
     * 예: AUTH-401-1 등
     * @return 고유 에러 코드
     */
    String getCode();

    /**
     * 에러 메시지
     * - 클라이언트 응답 또는 로그 메시지
     * @return 에러 메시지
     */
    String getMessage();
}
