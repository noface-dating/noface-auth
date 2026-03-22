package com.duri.duriauth.security.handler;

import com.duri.duriauth.dto.response.LoginResponseDto;
import com.duri.duriauth.service.AuthService;
import com.duri.duriauth.web.cookie.CookieService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

/**
 * Spring Security 로그인 성공 시 호출되는 핸들러
 *
 * <p>
 *     - 인증에 성공한 사용자에게 JWT Access Token을 쿠키에 담아 응답
 *     - 로그인 성공 메시지를 JSON 형태로 반환
 * </p>
 */
@RequiredArgsConstructor
@Component
public class UserLoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AuthService authService;
    private final CookieService cookieService;
    private final ObjectMapper objectMapper;

    /**
     * 로그인 성공 시 호출
     *
     * <p>
     *     - JWT Access Token 발급 및 쿠키 설정
     *     - JSON 응답
     * </p>
     *
     * @param request HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @param authentication 인증 성공 정보
     * @throws IOException HTTP 응답 처리 중 예외 발생 시
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException
    {
        // 1. JWT Access Token 생성
        String accessToken = authService.generateAccessToken(authentication);

        // 2. Cookie 응답 설정
        cookieService.addAccessTokenCookie(response, accessToken);

        // 3. 로그인 성공 응답 DTO 생성
        LoginResponseDto loginResponseDto = new LoginResponseDto("로그인 성공");

        // 4. HTTP 응답 설정
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);

        // 5. JSON 응답
        objectMapper.writeValue(response.getWriter(), loginResponseDto);
    }
}
