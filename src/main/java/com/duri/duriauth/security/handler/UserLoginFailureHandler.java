package com.duri.duriauth.security.handler;

import com.duri.duriauth.dto.response.ErrorResponseDto;
import com.duri.duriauth.exception.AuthErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

/**
 * Spring Security 로그인 실패 시 호출되는 핸들러
 *
 * <p>
 *     - 인증 실패 시 공통 에러 코드를 기반으로 JSON 형태의 오류 응답 반환
 * </p>
 */
@RequiredArgsConstructor
@Component
public class UserLoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    /**
     * 로그인 실패 시 호출되며, 실패 사유에 관계없이 공통 에러 코드(INVALID_CREDENTIALS)와 메시지를 JSON 형식으로 응답
     *
     * @param request HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @param exception 인증 실패 예외
     * @throws IOException HTTP 응답 처리 중 예외 발생 시
     */
    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception)
            throws IOException
    {
        // 1. 예외 - 에러 코드 매핑
        AuthErrorCode errorCode = this.mapToAuthErrorCode(exception);

        // 2. 예외 응답 DTO 생성
        ErrorResponseDto errorResponseDto = new ErrorResponseDto(
                errorCode.getCode(),
                errorCode.getMessage()
        );

        // 3. HTTP 응답 설정
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(errorCode.getStatus().value());

        // 4. JSON 응답
        objectMapper.writeValue(response.getWriter(), errorResponseDto);
    }

    /**
     * 인증 실패 예외를 공통 인증 오류 코드로 매핑
     *
     * <p>
     *     - 모든 로그인 실패 예외를 INVALID_CREDENTIALS로 처리 (401 Unauthorized)
     * </p>
     * @param exception 인증 실패 예외
     * @return AuthErrorCode 인증 실패 코드
     */
    private AuthErrorCode mapToAuthErrorCode(AuthenticationException exception) {
        // 로그인 실패 이유 구분하지 않음
        return AuthErrorCode.INVALID_CREDENTIALS;
    }
}
