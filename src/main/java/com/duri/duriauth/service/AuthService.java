package com.duri.duriauth.service;

import com.duri.duriauth.domain.TokenPair;
import com.duri.duriauth.dto.request.LoginRequest;
import com.duri.duriauth.entity.UserRole;
import com.duri.duriauth.entity.User;
import com.duri.duriauth.exception.AuthErrorCode;
import com.duri.duriauth.exception.AuthException;
import com.duri.duriauth.provider.JwtTokenProvider;
import com.duri.duriauth.repository.UserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 인증/인가 도메인의 핵심 비즈니스 로직 서비스
 *
 * <p>
 *     - 사용자 로그인 처리 : 사용자 인증 정보 검증 및 토큰 발급
 *     - 로그인 성공 시 TokenPair 생성
 *     - 로그인 실패 시 AuthException 발생
 * </p>
 */

@RequiredArgsConstructor
@Service
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
//    private final AuthRedisService redisService;

    /**
     * 사용자 로그인 처리
     *
     * <p>
     *     - username 기반 사용자 조회
     *     - 비밀번호 검증
     *     - 인증 성공 시 Access Token / Refresh Token 발급
     * </p>
     *
     * <p>
     *     - 사용자 인증 실패 조건 :
     *     - 존재하지 않는 사용자 (username 불일치(존재X))
     *     - 비밀번호 불일치
     * </p>
     * @param request 로그인 요청 정보 (username, password)
     * @return Access Token / Refresh Token이 포함된 TokenPair
     * @throws AuthException 사용자 인증 실패 시 발생
     */
    public TokenPair userLogin(LoginRequest request) {
        // 사용자 조회
        Optional<User> optionalUser = userRepository.findByUsername(request.username());
        if (optionalUser.isEmpty()) {
            // 사용자 존재하지 않는 경우, 로그인 실패
            throw new AuthException(AuthErrorCode.INVALID_CREDENTIALS);
        }

        // 사용자 존재하는 경우
        User user = optionalUser.get();
        // TODO: PasswordEncoder 적용
        if (! request.password().equals(user.getPassword())) {
            // 비밀번호 일치하지 않는 경우, 로그인 실패
            throw new AuthException(AuthErrorCode.INVALID_CREDENTIALS);
        }

        // 비밀번호 일치하는 경우, 로그인 성공
        // 동시 로그인 허용O (무제한)
        // TODO: DB userId (Long --> String(UUID)) 타입 수정 이후 해당 코드 수정 필요
        String userId = user.getUserId().toString();    // 임시로 문자열로 변환
        UserRole userRole = user.getRole();

        return jwtTokenProvider.generateTokenPair(userId, userRole);
    }
}
