package com.duri.duriauth.web.cookie;

import com.duri.duriauth.common.properties.CookieProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

/**
 * HTTP Cookie 생성 및 관리 서비스 (Infrastructure Service)
 *
 * <p>
 *     - Access Token / Refresh Token 쿠키 설정(추가) 및 삭제 처리 지원
 *     - CookieProperties 설정 기반으로 쿠키 보안 옵션 적용
 *     - Access Token / Refresh Token 쿠키 경로 다르게 적용
 * </p>
 *
 * <p>
 *     - domain, path, httpOnly, secure, sameSite 등의 정책을 중앙에서 관리
 *     - 클라이언트 응답 시 Set-Cookie 헤더를 통해 쿠키 전달
 * </p>
 */
@RequiredArgsConstructor
@Service
public class CookieService {

    private static final String ACCESS_TOKEN_COOKIE_NAME = "access_token";
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";

    private final CookieProperties cookieProperties;

    /**
     * Access Token 쿠키 추가
     *
     * <p>
     *     - 로그인 및 토큰 재발급 시 사용
     *     - HttpOnly Cookie로 설정
     * </p>
     * @param response HTTP 응답 객체
     * @param token Access Token 값
     */
    public void addAccessTokenCookie(HttpServletResponse response, String token) {
        this.buildCookie(
                response,
                ACCESS_TOKEN_COOKIE_NAME,
                token,
                cookieProperties.getAccessPath(),
                cookieProperties.getAccessMaxAge()
        );
    }

    /**
     * Refresh Token 쿠키 추가
     *
     * <p>
     *     - 로그인 및 토큰 재발급 시 사용
     *     - HttpOnly Cookie로 설정
     * </p>
     * @param response HTTP 응답 객체
     * @param token Refresh Token 값
     */
    public void addRefreshTokenCookie(HttpServletResponse response, String token) {
        this.buildCookie(
                response,
                REFRESH_TOKEN_COOKIE_NAME,
                token,
                cookieProperties.getRefreshPath(),
                cookieProperties.getRefreshMaxAge()
        );
    }

    /**
     * Access Token 쿠키 삭제
     *
     * <p>
     *     - 로그아웃 및 인증 해제 시 사용
     *     - maxAge=0 설정으로 브라우저에서 즉시 삭제
     * </p>
     * @param response HTTP 응답 객체
     */
    public void deleteAccessTokenCookie(HttpServletResponse response) {
        this.buildCookie(
                response,
                ACCESS_TOKEN_COOKIE_NAME,
                "",
                cookieProperties.getAccessPath(),
                0
        );
    }

    /**
     * Refresh Token 쿠키 삭제
     *
     * <p>
     *     - 로그아웃 시 Refresh Token 제거
     *     - maxAge=0 설정으로 브라우저에서 즉시 삭제
     * </p>
     *
     * @param response HTTP 응답 객체
     */
    public void deleteRefreshTokenCookie(HttpServletResponse response) {
        this.buildCookie(
                response,
                REFRESH_TOKEN_COOKIE_NAME,
                "",
                cookieProperties.getRefreshPath(),
                0
        );
    }

    /**
     * Access Token Cookie 조회
     *
     * <p>
     *     - HTTP 요청에 포함된 Cookie 목록에서 Access Token 값을 조회함
     *     - Access Token 쿠키가 존재하지 않는 경우, Optional.empty()를 반환함
     * </p>
     *
     * <p>
     *     - 주로 JwtAuthenticationFilter에서 인증 처리 시 사용됨
     * </p>
     *
     * @param request HTTP 요청 객체
     * @return Access Token 값 (존재하지 않을 경우, Optional.empty())
     */
    public Optional<String> getAccessToken(HttpServletRequest request) {
        return this.getCookieValue(request, ACCESS_TOKEN_COOKIE_NAME);
    }

    /**
     * Refresh Token Cookie 조회
     *
     * <p>
     *     - HTTP 요청에 포함된 Cookie 목록에서 Refresh Token 값을 조회함
     *     - Refresh Token 쿠키가 존재하지 않는 경우, Optional.empty()를 반환함
     * </p>
     *
     * <p>
     *     - 주로 JwtAuthenticationFilter에서 인증 처리 시 사용됨
     * </p>
     *
     * @param request HTTP 요청 객체
     * @return Refresh Token 값 (존재하지 않을 경우, Optional.empty())
     */
    public Optional<String> getRefreshToken(HttpServletRequest request) {
        return this.getCookieValue(request, REFRESH_TOKEN_COOKIE_NAME);
    }

    /**
     * Cookie 생성 및 Set-Cookie 헤더 추가
     *
     * <p>
     *     - ResponseCookie Builder 사용해 쿠키 생성
     *     - CookieProperties 설정 기반으로 보안 옵션 적용
     * </p>
     *
     * <p>
     *     - 적용되는 주요 옵션 :
     *     - domain / path / httpOnly / secure (HTTP, HTTPS) / sameSite / maxAge
     * </p>
     *
     * @param response HTTP 응답 객체
     * @param name 쿠키 이름
     * @param value 쿠키 값 (토큰)
     * @param path 쿠키 적용 경로
     * @param maxAge 쿠키 만료시간
     */
    private void buildCookie(
            HttpServletResponse response,
            String name,
            String value,
            String path,
            int maxAge
    )
    {
        // TODO: secure: true (HTTPS) 적용
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, value)
                .domain(cookieProperties.getDomain())
                .path(path)
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .maxAge(maxAge);

        ResponseCookie cookie = builder.build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    /**
     * 지정된 이름의 Cookie 값 조회
     *
     * <p>
     *     - HTTP 요청에 포함된 Cookie 목록을 순회하며 지정된 이름의 Cookie 값을 반환함
     * </p>
     *
     * <p>
     *     - Cookie가 존재하지 않거나, 값이 null인 경우 Optional.empty()를 반환함
     * </p>
     *
     * @param request HTTP 요청 객체
     * @param cookieName 조회할 Cookie 이름
     * @return Cookie 값 (존재하지 않을 경우 Optional.empty())
     */
    private Optional<String> getCookieValue(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (Objects.isNull(cookies)) {
            return  Optional.empty();
        }

        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName())) {
                return Optional.ofNullable(cookie.getValue());
            }
        }

        return Optional.empty();
    }

}
