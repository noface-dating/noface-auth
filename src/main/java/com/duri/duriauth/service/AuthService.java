package com.duri.duriauth.service;

import com.duri.duriauth.domain.TokenPair;
import com.duri.duriauth.dto.request.LoginRequest;
import com.duri.duriauth.entity.Role;
import com.duri.duriauth.entity.User;
import com.duri.duriauth.provider.JwtTokenProvider;
import com.duri.duriauth.repository.UserRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;

    public TokenPair userLogin(LoginRequest request) {
        // 사용자 조회
        Optional<User> optionalUser = userRepository.findByUsername(request.username());
        if (optionalUser.isEmpty()) {
            // 사용자 존재하지 않는 경우, 로그인 실패
            // TODO: 커스텀 예외 던지기
            return null;
        }

        // 사용자 존재하는 경우
        User user = optionalUser.get();
        // TODO: PasswordEncoder 사용
        if (! request.password().equals(user.getPassword())) {
            // 비밀번호 일치하지 않는 경우, 로그인 실패
            // TODO: 커스텀 예외 던지기
            return null;
        }

        // 비밀번호 일치하는 경우, 로그인 성공
        // TODO: JwtTokenProvider - TokenPair 생성하는 메서드 추가
        String sessionId = UUID.randomUUID().toString();
        String accessJti = UUID.randomUUID().toString();
        String refreshJti = UUID.randomUUID().toString();

        String accessToken = jwtTokenProvider.generateAccessToken(accessJti);
        String refreshToken = jwtTokenProvider.generateRefreshToken(refreshJti, sessionId);

        // TODO: RedisService 분리

        // 1. ROOT Session 저장
        this.createAndSaveRootSession(sessionId, user.getUsername());

        // 2. Refresh Session 저장
        this.saveRefresh(refreshJti, sessionId);

        // 3. Access Session 저장
        this.saveAccess(accessJti, sessionId);

        // 4. Authority Session 저장
        this.saveAuthorities(sessionId, user.getRole());

        return new TokenPair(accessToken, refreshToken);
    }

    private void createAndSaveRootSession (String sessionId, String username) {
        String key = "auth:session:" + sessionId;

        Instant now = Instant.now();

        redisTemplate.opsForHash().put(key, "sub", username);
        redisTemplate.opsForHash().put(key, "createdAt", now.toString());   // ISO-8601
        redisTemplate.opsForHash().put(key, "status", "ACTIVE");
        redisTemplate.expire(key, 6, TimeUnit.HOURS);
    }

    private void saveRefresh (String refreshJti, String sessionId) {
        String key = "auth:refresh:" + refreshJti;
        redisTemplate.opsForHash().put(key, "sessionId", sessionId);
        redisTemplate.opsForHash().put(key, "status", "ACTIVE");
        redisTemplate.expire(key, 30, TimeUnit.MINUTES);
    }

    private void saveAccess (String accessJti, String sessionId) {
        String key = "auth:access:" + accessJti;
        redisTemplate.opsForHash().put(key, "sessionId", sessionId);
        redisTemplate.expire(key, 10, TimeUnit.MINUTES);
    }

    private void saveAuthorities (String sessionId, Role role) {
        String key = "auth:authority:" + sessionId;
        redisTemplate.opsForHash().put(key, "role", role.name());
        redisTemplate.expire(key, 6, TimeUnit.HOURS);
    }
}
