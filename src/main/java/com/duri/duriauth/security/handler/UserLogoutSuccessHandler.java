package com.duri.duriauth.security.handler;

import com.duri.duriauth.web.cookie.CookieService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

/**
 * Spring Security 로그아웃 성공 시 호출되는 핸들러
 *
 * <p>
 *     - 로그아웃 시 클라이언트의 Access Token 쿠키 삭제
 *     - MVP) 로그아웃 시 홈 화면으로 리다이렉션 처리
 * </p>
 */
@RequiredArgsConstructor
@Component
public class UserLogoutSuccessHandler implements LogoutSuccessHandler {

    private final CookieService cookieService;

    /**
     * 로그아웃 성공 시 호출되며, 쿠키 삭제 및 홈 화면으로 리다이렉션 처리
     *
     * @param request HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @param authentication 인증 정보 (로그아웃 시 null일 수 있음)
     */
    @Override
    public void onLogoutSuccess(HttpServletRequest request,
                                HttpServletResponse response,
                                @Nullable Authentication authentication)
        throws IOException
    {
        // 1. Cookie 삭제 (MVP: ONLY Access Token)
        cookieService.deleteAccessTokenCookie(response);

        // 2. 리다이렉션 (홈 화면)
        response.sendRedirect("http://localhost:8080/");

        // HTTP 응답 설정 (MVP 버전에서는 사용X)
        // response.setContentType("application/json");
        // response.setCharacterEncoding("UTF-8");
        // response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }
}
