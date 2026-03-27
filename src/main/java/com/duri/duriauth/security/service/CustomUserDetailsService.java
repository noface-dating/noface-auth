package com.duri.duriauth.security.service;

import com.duri.duriauth.entity.User;
import com.duri.duriauth.repository.UserRepository;
import com.duri.duriauth.security.principal.CustomUserDetails;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Spring Security에서 사용자 인증 시 호출되는 서비스
 *
 * <p>
 *      - AuthenticationManager > DaoAuthenticationProvider가 해당 서비스 호출하여 사용자 인증 수행
 *      - DB에서 사용자 정보를 조회하고 권한을 설정하여 UserDetails 객체 반환
 * </p>
 */
@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {
    private static final String ROLE_PREFIX = "ROLE_";

    private final UserRepository userRepository;

    /**
     * 주어진 사용자 로그인 아이디(username)로 사용자 정보를 조회하고 UserDetails 객체 생성
     *
     * <p>
     *      - 조회된 사용자 정보가 없으면 UsernameNotFoundException 발생시킴
     * </p>
     *
     * @param username 인증할 사용자 로그인 아이디
     * @return 인증된 사용자 정보를 담은 UserDetails 객체
     * @throws UsernameNotFoundException 사용자 정보를 찾지 못한 경우 발생
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // 1. 사용자 조회
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        // 2. 권한 설정
        List<SimpleGrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority(ROLE_PREFIX + user.getRole().name()));

        // 3. CustomUserDetails 생성
        return new CustomUserDetails(
                user.getUserId(),
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }
}
