package com.duri.duriauth.exception;

import com.duri.duriauth.dto.response.ErrorResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 전역 예외 처리 핸들러
 *
 * <p>
 *     애플리케이션 전반에서 발생하는 예외를 일관된 ErrorResponse 형태로 변환하여 클라이언트에 반환한다.
 * </p>
 */

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ErrorResponseDto> handleAuthException(AuthException e) {
        BaseErrorCode errorCode = e.getErrorCode();

        log.warn("[AuthException] code: {}, message: {}",
                errorCode.getCode(), errorCode.getMessage());

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponseDto.from(errorCode));
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleUnexpectedException(Exception e) {
        BaseErrorCode errorCode = AuthErrorCode.INTERNAL_SERVER_ERROR;

        log.error("[Unexpected Exception] code: {}, message: {}",
                errorCode.getCode(), errorCode.getMessage(), e);

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponseDto.from(errorCode));
    }
}
