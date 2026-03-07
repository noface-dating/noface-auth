package com.duri.duriauth.provider;

import static com.duri.duriauth.domain.TokenType.ACCESS;
import static com.duri.duriauth.domain.TokenType.REFRESH;

import com.duri.duriauth.common.properties.JwtProperties;
import com.duri.duriauth.domain.TokenPair;
import com.duri.duriauth.entity.UserRole;
import com.duri.duriauth.exception.logging.JwtTokenGenerationException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Jwts.SIG;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * JWT 생성 및 검증을 담당하는 클래스
 *
 * <p>
 *     - Access Token / Refresh Token 생성
 *     - ES256 서명 처리
 *     - 토큰 구조, 서명, 발급자 및 만료시간 검증
 * </p>
 *
 * <p>
 *     - sub(userId) + role을 포함하여 Gateway에서 DB/Redis 조회 없이 검증 및 인가 처리
 *     - Redis는 Refresh Token 블랙리스트 용도로만 사용
 * </p>
 */

@RequiredArgsConstructor
@Component
public class JwtTokenProvider {

    private static final String ROLE = "role";
    private static final String TOKEN_TYPE = "token_type";

    private final JwtKeyProvider jwtKeyProvider;
    private final JwtProperties jwtProperties;

    /**
     * Access Token 및 Refresh Token 생성
     *
     * <p>
     *     - 내부적으로 각각의 고유 식별자(jti) 생성
     *     - Access Token에는 사용자 권한(role)을 포함
     *     - Refresh Token은 재발급 전용으로 사용
     * </p>
     *
     * <p>
     *     - 생성된 두 토큰은 TokenPair로 묶어 반환함
     * </p>
     * @param userId 사용자 식별자
     * @param userRole 사용자 권한
     * @return Access Token과 Refresh Token이 포함된 TokenPair
     */

    public TokenPair generateTokenPair(String userId, UserRole userRole) {
        String accessJti = UUID.randomUUID().toString();
        String refreshJti = UUID.randomUUID().toString();

        String accessToken = generateAccessToken(accessJti, userId, userRole);
        String refreshToken = generateRefreshToken(refreshJti, userId);

        return new TokenPair(accessToken, refreshToken);
    }

    /**
     * Access Token 생성
     *
     * <p>
     *     - sub(userId) + role 포함
     *     - Gateway에서 직접 검증 및 인가 처리 가능
     *     - 요청 처리 시 Redis 조회 없음
     * </p>
     *
     * @param accessJti Access Token 식별자(UUID)
     * @param userId 사용자 식별자
     * @param userRole 사용자 권한
     * @return 서명된 Access Token 문자열
     */
    private String generateAccessToken(String accessJti, String userId, UserRole userRole) {
        try {
            Instant now = Instant.now();
            Instant expiration = now.plus(jwtProperties.getAccess().getValidity());

            return Jwts.builder()
                    .id(accessJti)
                    .claim(TOKEN_TYPE, ACCESS.name())
                    .issuer(jwtProperties.getIssuer())
                    .subject(userId)
                    .claim(ROLE, userRole.name())
                    .issuedAt(Date.from(now))
                    .expiration(Date.from(expiration))
                    .signWith(jwtKeyProvider.getPrivateKey(), SIG.ES256)
                    .compact();
        } catch (Exception e) {
            throw new JwtTokenGenerationException("Access Token 생성 실패", e);
        }

    }

    /**
     * Refresh Token 생성
     *
     * <p>
     *     - 재발급 전용 토큰
     *     - 블랙리스트 기반 로그아웃 통제
     * </p>
     *
     * @param refreshJti Refresh Token 식별자(UUID)
     * @param userId 사용자 식별자
     * @return 서명된 Refresh Token 문자열
     */
    private String generateRefreshToken(String refreshJti, String userId) {
        try {
            Instant now = Instant.now();
            Instant expiration = now.plus(jwtProperties.getRefresh().getValidity());

            return Jwts.builder()
                    .id(refreshJti)
                    .claim(TOKEN_TYPE, REFRESH.name())
                    .issuer(jwtProperties.getIssuer())
                    .subject(userId)
                    .issuedAt(Date.from(now))
                    .expiration(Date.from(expiration))
                    .signWith(jwtKeyProvider.getPrivateKey(), SIG.ES256)
                    .compact();
        } catch (Exception e) {
            throw new JwtTokenGenerationException("Refresh Token 생성 실패", e);
        }

    }

    // TODO: Token Parsing & Validation 로직 작성

}
