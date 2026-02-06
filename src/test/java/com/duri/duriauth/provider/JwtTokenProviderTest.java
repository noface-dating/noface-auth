package com.duri.duriauth.provider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        // Access Token: 10분
        // Refresh Token: 6시간
        jwtTokenProvider = new JwtTokenProvider(
                10 * 60 * 1000L,
                6 * 60 * 60 * 1000L
        );
    }

    @Test
    @DisplayName("Access Token 생성 성공")
    void generateAccessToken_success() {
        String accessToken = jwtTokenProvider.generateAccessToken();

        assertNotNull(accessToken);
        assertFalse(accessToken.isBlank());
    }

    @Test
    @DisplayName("Refresh Token 생성 성공")
    void generateRefreshToken_success() {
        String refreshToken = jwtTokenProvider.generateRefreshToken();

        assertNotNull(refreshToken);
        assertFalse(refreshToken.isBlank());
    }

    @Test
    @DisplayName("Access Token 생성 후 검증 성공")
    void generateAndValidateAccessToken() {
        String accessToken = jwtTokenProvider.generateAccessToken();

        boolean isValid = jwtTokenProvider.validateToken(accessToken);

        assertTrue(isValid);
    }

    @Test
    @DisplayName("Refresh Token 생성 후 검증 성공")
    void generateAndValidateRefreshToken() {
        String refreshToken = jwtTokenProvider.generateRefreshToken();

        boolean isValid = jwtTokenProvider.validateToken(refreshToken);

        assertTrue(isValid);
    }

    @Test
    @DisplayName("위조된 토큰 검증 실패")
    void invalidToken_shouldFail() {
        String fakeToken = "fake-token";

        boolean isValid = jwtTokenProvider.validateToken(fakeToken);

        assertFalse(isValid);
    }

    @Test
    @DisplayName("만료된 토큰 검증 실패")
    void expiredToken_shouldFail() throws InterruptedException {
        JwtTokenProvider expiredTokenProvider = new JwtTokenProvider(
                1L,
                1L
        );

        String expiredToken = expiredTokenProvider.generateAccessToken();

        Thread.sleep(10);

        boolean isValid = expiredTokenProvider.validateToken(expiredToken);

        assertFalse(isValid);
    }
}
