package com.duri.duriauth.service;

import com.duri.duriauth.domain.TokenPair;
import com.duri.duriauth.dto.request.LoginRequest;
import com.duri.duriauth.entity.User;
import com.duri.duriauth.provider.JwtTokenProvider;
import com.duri.duriauth.repository.UserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

//    public AuthService(JwtTokenProvider jwtTokenProvider, UserRepository userRepository) {
//        this.jwtTokenProvider = jwtTokenProvider;
//        this.userRepository = userRepository;
//    }

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
        String accessToken = jwtTokenProvider.generateAccessToken();
        String refreshToken = jwtTokenProvider.generateRefreshToken();
        return new TokenPair(accessToken, refreshToken);
    }

}
