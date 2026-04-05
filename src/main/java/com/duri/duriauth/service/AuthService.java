package com.duri.duriauth.service;

import com.duri.duriauth.entity.UserRole;
import com.duri.duriauth.provider.JwtTokenProvider;
import com.duri.duriauth.security.principal.CustomUserDetails;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * 인증 관련 서비스
 *
 * <p>
 *     - MVP : JWT 토큰 관리 책임
 * </p>
 */
@RequiredArgsConstructor
@Service
public class AuthService {

    private static final String ROLE_PREFIX = "ROLE_";

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 인증된 사용자 정보를 기반으로 JWT Access Token 생성 및 반환
     *
     * @param authentication 인증된 사용자 정보가 담긴 Authentication 객체
     * @return 생성된 JWT Access Token 문자열
     */
    public String generateAccessToken(Authentication authentication) {
        // 1. Principal 추출
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // 2. 필요한 값 추출
        String userId = userDetails.getUserId();
        UserRole userRole = this.extractRole(userDetails);

        // 3. JWT Access Token 생성
        return jwtTokenProvider.generateAccessToken(userId, userRole);
    }

    /**
     * CustomUserDetails에서 사용자 권한 추출
     *
     * @param userDetails 인증된 사용자 정보
     * @return 사용자 권한 (UserRole)
     * @throws IllegalStateException 권한 정보가 없는 경우 예외 발생
     */
    private UserRole extractRole(CustomUserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .findFirst()
                .map(authority -> UserRole.valueOf(
                        authority.getAuthority().replace(ROLE_PREFIX, "")
                ))
                .orElseThrow(() -> new IllegalStateException("권한 정보 없음"));
    }
}
