package com.duri.duriauth.controller;

import com.duri.duriauth.domain.TokenPair;
import com.duri.duriauth.dto.request.LoginRequest;
import com.duri.duriauth.dto.response.LoginResponse;
import com.duri.duriauth.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/* TEST Controller */
@RequiredArgsConstructor
@RestController
public class AuthController {

    private final AuthService authService;

    // Spring Security Config TEST API
    @GetMapping("/actuator/test")
    public String actuatorTest() {
        return "공개 API 접속 : /actuator/** 테스트 성공";
    }

    @GetMapping("/health")
    public String health() {
        return "공개 API 접속 : /health 테스트 성공";
    }

    @GetMapping("/auth/test")
    public String authTest() {
        return "공개 API 접속 : /auth/** 테스트 성공";
    }

    // Login TEST API
    @PostMapping("/auth/login")
    public void userLogin(@Valid @RequestBody LoginRequest request,
                                                   HttpServletResponse response) throws IOException
    {
        TokenPair tokenPair = authService.userLogin(request);
        if (Objects.isNull(tokenPair)) {
            // 로그인 실패 예외 처리
            this.clearCookies(response);

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("text/html; charset=UTF-8");
            response.getWriter().write(
                    "<!DOCTYPE html>" +
                            "<html><body>" +
                            "<h1>로그인 실패</h1>" +
                            "<p>ID 또는 비밀번호가 올바르지 않습니다.</p>" +
                            "</body></html>"
            );
            return;

        }

        // 로그인 성공
        Cookie accessCookie = new Cookie("ACCESS_TOKEN", tokenPair.accessToken());
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(false);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(10 * 60);    // 10분
        response.addCookie(accessCookie);

        Cookie refreshCookie = new Cookie("REFRESH_TOKEN", tokenPair.refreshToken());
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(30 * 60);   // 30분
        response.addCookie(refreshCookie);

        // TODO: 인증된 사용자 정보 조회 API 분리 (GET /me)
        String username = request.username();
        response.setContentType("text/html; charset=UTF-8");
        response.getWriter().write(
                "<!DOCTYPE html>" +
                        "<html lang='ko'>" +
                        "<head><meta charset='UTF-8'><title>로그인 성공</title></head>" +
                        "<body>" +
                        "<h1>로그인 성공!</h1>" +
                        "<p>사용자: " + username + "</p>" +
                        "</body>" +
                        "</html>"
        );
    }

    private void clearCookies(HttpServletResponse response) {
        Cookie accessCookie = new Cookie("ACCESS_TOKEN", null);
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(false);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(0);
        response.addCookie(accessCookie);

        Cookie refreshCookie = new Cookie("REFRESH_TOKEN", null);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(0);
        response.addCookie(refreshCookie);
    }

}
