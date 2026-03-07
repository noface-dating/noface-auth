package com.duri.duriauth.controller;

import com.duri.duriauth.domain.TokenPair;
import com.duri.duriauth.dto.request.LoginRequest;
import com.duri.duriauth.dto.response.LoginResponse;
import com.duri.duriauth.service.AuthService;
import com.duri.duriauth.web.cookie.CookieService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 인증/인가 HTTP API 컨트롤러
 *
 * <p>
 *     - /auth/** 경로 하위에서 인증 관련 API 제공
 * </p>
 *
 * <p>
 *     - 사용자 로그인 처리 :
 *     - 사용자 인증 정보 검증 및 토큰 발급
 *     - 발급된 토큰을 Cookie로 전달
 * </p>
 *
 * <p>
 *     - 인증 및 토큰 발급은 AuthService에 위임
 *     - 토큰 저장(전달)은 CookieService에 위임
 * </p>
 */

// TODO: 로그인/로그아웃 이벤트 발행(RabbitMQ) 필요?

@RequestMapping("/auth")
@RequiredArgsConstructor
@RestController
public class AuthController {

    private final AuthService authService;
    private final CookieService cookieService;

    // Spring Security Config TEST API
    @GetMapping("/test")
    public String authTest() {
        return "공개 API 접속 : /auth/** 테스트 성공";
    }

    /**
     * 사용자 로그인 API
     *
     * <p>
     *     - 로그인 흐름 :
     *     - 1. 로그인 요청 (username, password) 검증
     *     - 2. AuthService: 사용자 인증 정보 검증 및 토큰 발급
     *     - 3. 발급된 토큰을 HttpOnly Cookie로 설정
     * </p>
     *
     * <p>
     *     - 로그인 실패 처리 :
     *     - AuthService 내부에서 AuthException 발생
     *     - 공통 예외 처리 로직에서 응답 반환
     * </p>
     * @param request 로그인 요청 DTO
     * @param response HTTP 응답 객체 (Cookie 설정)
     * @return HTTP 200 OK 응답
     */

    // Login TEST API
    @PostMapping("/login")
    public ResponseEntity<Void> userLogin(@Valid @RequestBody LoginRequest request,
                                          HttpServletResponse response)
    {
        TokenPair tokenPair = authService.userLogin(request);

        // 로그인 실패: AuthService - 공통 응답 처리

        // 로그인 성공
        // TODO: 인증된 사용자 정보 조회 API 분리 (GET /me)
        cookieService.addAccessTokenCookie(response, tokenPair.accessToken());
        cookieService.addRefreshTokenCookie(response, tokenPair.refreshToken());

        return ResponseEntity.ok().build();
    }
}
