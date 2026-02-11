package com.duri.duriauth.provider;

import com.duri.duriauth.common.properties.JwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Jwts.SIG;
import java.time.Instant;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class JwtTokenProvider {

    private final JwtKeyProvider jwtKeyProvider;
    private final JwtProperties jwtProperties;

    public String generateAccessToken(String accessJti) {
        Instant now = Instant.now();
        Instant expiration = now.plusMillis(jwtProperties.getAccess().getValidity());

        return Jwts.builder()
                .id(accessJti)
                .issuer(jwtProperties.getIssuer())
                .claim("token_type", "ACCESS")
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(jwtKeyProvider.getPrivateKey(), SIG.ES256)
                .compact();
    }

    public String generateRefreshToken(String refreshJti, String sessionId) {
        Instant now = Instant.now();
        Instant expiration = now.plusMillis(jwtProperties.getRefresh().getValidity());

        return Jwts.builder()
                .id(refreshJti)
                .issuer(jwtProperties.getIssuer())
                .claim("session_id", sessionId)
                .claim("token_type", "REFRESH")
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(jwtKeyProvider.getPrivateKey(), SIG.ES256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            // 토큰 구조, 서명, 발급자 및 만료시간 검증
            Jwts.parser()
                    .verifyWith(jwtKeyProvider.getPublicKey())
                    .requireIssuer(jwtProperties.getIssuer())
                    .build()
                    .parseSignedClaims(token);

            return true;
        } catch (Exception e) {
            // TODO: 토큰 예외 세분화 및 인증 실패 사유 분리
            return false;
        }
    }

}
