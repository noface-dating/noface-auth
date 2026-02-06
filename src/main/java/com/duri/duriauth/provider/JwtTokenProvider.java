package com.duri.duriauth.provider;

import io.jsonwebtoken.Jwts;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

// TODO: @Component Bean으로 전환
// TODO: JwtProperty, JwtKeyProvider 등으로 책임 분리
public class JwtTokenProvider {

    private final ECPrivateKey privateKey;
    private final ECPublicKey publicKey;

    private final long accessTokenValidity;
    private final long refreshTokenValidity;

    public JwtTokenProvider(long accessTokenValidity, long refreshTokenValidity)
    {
        // TODO: EC KeyPair 직접 생성 대신 외부 설정으로 분리 + Spring Bean 주입받도록 수정
        KeyPair keyPair = this.generateEcKeyPair();
        this.privateKey = (ECPrivateKey) keyPair.getPrivate();
        this.publicKey = (ECPublicKey) keyPair.getPublic();

        this.accessTokenValidity = accessTokenValidity;
        this.refreshTokenValidity = refreshTokenValidity;
    }

    public String generateAccessToken() {
        Instant now = Instant.now();
        Instant expiration = now.plusMillis(accessTokenValidity);

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(privateKey)
                .compact();
    }

    public String generateRefreshToken() {
        Instant now = Instant.now();
        Instant expiration = now.plusMillis(refreshTokenValidity);

        // TODO: 로그인 성공 시 생성된 Redis sessionId를 파라미터로 전달받도록 수정
        String sessionId = "session-id";

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .claim("session_id", sessionId)
                .claim("token_type", "REFRESH")
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(privateKey)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token);  // 토큰 구조, 서명, 만료시간 검증

            return true;
        } catch (Exception e) {
            // TODO: 토큰 예외 세분화 및 인증 실패 사유 분리
            return false;
        }
    }

    private KeyPair generateEcKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
            keyPairGenerator.initialize(256);
            return keyPairGenerator.generateKeyPair();
        } catch (Exception e) {
            throw new IllegalStateException("EC 키 생성 실패", e);
        }
    }
}
