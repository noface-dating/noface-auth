package com.duri.duriauth.provider;

import static com.duri.duriauth.domain.TokenType.ACCESS;
import static com.duri.duriauth.domain.TokenType.REFRESH;

import com.duri.duriauth.common.properties.JwtProperties;
import com.duri.duriauth.domain.TokenPair;
import com.duri.duriauth.domain.TokenType;
import com.duri.duriauth.entity.UserRole;
import com.duri.duriauth.exception.AuthErrorCode;
import com.duri.duriauth.exception.AuthException;
import com.duri.duriauth.exception.logging.JwtTokenGenerationException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Jwts.SIG;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;
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
 *     - 추후 Redis는 Refresh Token 블랙리스트 용도로만 사용
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
     * Access Token 생성 (MVP)
     *
     * <p>
     *     - 내부적으로 각각의 고유 식별자(jti) 생성
     *     - Access Token에는 사용자 권한(role)을 포함
     *     - MVP : Refresh Token 사용X
     * </p>
     *
     * @param userId 사용자 식별자
     * @param userRole 사용자 권한
     * @return Access Token
     */
    public String generateAccessToken(String userId, UserRole userRole) {
        String accessJti = UUID.randomUUID().toString();
        String accessToken = generateAccessToken(accessJti, userId, userRole);

        return accessToken;
    }

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
     * JWT 토큰을 파싱하여 Claims 추출
     *
     * <p>
     *     - 토큰의 서명(Signature)을 Public Key로 검증
     *     - 발급자(issuer) 검증
     *     - 토큰 구조 및 유효성 검증
     * </p>
     *
     * <p>
     *     - 만료된 토큰의 경우, EXPIRED_TOKEN 예외 발생
     *     - 그 외 유효하지 않은 토큰은 INVALID_TOKEN 예외 발생
     * </p>
     *
     * @param token JWT 토큰 문자열
     * @return 파싱된 Claims 객체
     * @throws AuthException 토큰이 만료된 경우 또는 유효하지 않은 경우
     */
    public Claims parseClaims(String token) {
        if (Objects.isNull(token) || token.isBlank()) {
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        }

        try {
            return Jwts.parser()
                    .verifyWith(jwtKeyProvider.getPublicKey())
                    .requireIssuer(jwtProperties.getIssuer())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new AuthException(AuthErrorCode.EXPIRED_TOKEN, e);
        } catch (JwtException e) {
            throw new AuthException(AuthErrorCode.INVALID_TOKEN, e);
        }
    }

    /**
     * Claims에서 사용자 식별자(userId)를 추출
     *
     * <p>
     *     - JWT 토큰의 sub(subject) 값을 반환
     *     - 토큰 파싱 과정에서 유효성 검증이 완료되었음을 전제로 함
     * </p>
     *
     * @param claims 파싱된 JWT Claims
     * @return 사용자 식별자 (userId)
     */
    public String getUserId(Claims claims) {
        // claims NULL 체크X (토큰 파싱 과정에서 예외 처리O)
        return claims.getSubject();
    }

    /**
     * Claims에서 사용자 권한(role)을 추출
     *
     * <p>
     *      - "role" Claim 값을 기반으로 UserRole 반환
     *      - 토큰 파싱 과정에서 유효성 검증이 완료되었음을 전제로 함
     *      - role 값이 없거나 Enum 변환에 실패할 경우 예외 발생
     * </p>
     *
     * @param claims 파싱된 JWT Claims
     * @return 사용자 권한 (userRole)
     * @throws AuthException role 값이 없거나 유효하지 않은 경우
     */
    public UserRole getRole(Claims claims) {
        try {
            // claims NULL 체크X (토큰 파싱 과정에서 예외 처리O)
            String role = claims.get(ROLE, String.class);

            if (Objects.isNull(role)) {
                throw new AuthException(AuthErrorCode.INVALID_TOKEN);
            }

            return UserRole.valueOf(role);
        } catch (IllegalArgumentException e) {
            throw new AuthException(AuthErrorCode.INVALID_TOKEN, e);
        }
    }

    /**
     * Claims에서 토큰 타입(TokenType)을 추출
     *
     * <p>
     *     - "token_type" Claim 값을 기반으로 TokenType 반환
     *     - Access Token / Refresh Token 구분에 사용
     *     - 토큰 파싱 과정에서 유효성 검증이 완료되었음을 전제로 함
     *     - token_type 값이 없거나 Enum 변환에 실패할 경우 예외 발생
     * </p>
     *
     * @param claims 파싱된 JWT Claims
     * @return 토큰 타입 (tokenType)
     * @throws AuthException token_type 값이 없거나 유효하지 않은 경우
     */
    public TokenType getTokenType(Claims claims) {
        try {
            // claims NULL 체크X (토큰 파싱 과정에서 예외 처리O)
            String tokenType = claims.get(TOKEN_TYPE, String.class);

            if (Objects.isNull(tokenType)) {
                throw new AuthException(AuthErrorCode.INVALID_TOKEN);
            }

            return TokenType.valueOf(tokenType);
        } catch (IllegalArgumentException e) {
            throw new AuthException(AuthErrorCode.INVALID_TOKEN, e);
        }
    }

    /**
     * Claims에서 JWT ID (jti)를 추출
     *
     * <p>
     *     - 토큰의 고유 식별자 (UUID) 반환
     *     - Refresh Token 블랙리스트 관리 및 추적에 사용할 예정
     *     - 토큰 파싱 과정에서 유효성 검증이 완료되었음을 전제로 함
     * </p>
     *
     * @param claims 파싱된 JWT Claims
     * @return JWT ID (jti)
     * @throws AuthException jti 값이 존재하지 않는 경우
     */
    public String getJti(Claims claims) {
        // claims NULL 체크X (토큰 파싱 과정에서 예외 처리O)
        String jti = claims.getId();

        if (Objects.isNull(jti)) {
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        }

        return jti;
    }

    /**
     * Access Token 생성
     *
     * <p>
     *     - sub(userId) + role 포함
     *     - ES256 Private Key로 JWT 서명
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
     *     - ES256 Private Key로 JWT 서명
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
}
