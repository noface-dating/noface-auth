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
 *     - MVP) 홈 화면으로 리다이렉션 처리
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
     *     - 홈 화면으로 리다이렉션 처리
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

        // 3. 200 OK 반환 → 프론트(login.html)의 fetch가 response.ok를 받고 window.location.href = '/' 처리
        // sendRedirect 사용 시 브라우저가 cross-origin 리다이렉트를 fetch로 따라가다 CORS 오류 발생
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
