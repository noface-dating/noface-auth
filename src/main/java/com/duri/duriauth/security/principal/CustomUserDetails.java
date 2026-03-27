package com.duri.duriauth.security.principal;

import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Spring Security에서 사용되는 사용자 정보를 담는 UserDetails 구현체
 *
 * <p>
 *     - AuthPrincipal을 구현하여 사용자 ID 제공
 *     - 권한과 인증 정보 포함
 * </p>
 */
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails, AuthPrincipal {

    private final String userId;
    private final String username;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;

    /**
     * 사용자 고유 식별자 반환
     *
     * @return 사용자 고유 식별자 (User ID)
     */
    @Override
    public String getUserId() {
        return userId;
    }

    /**
     * 사용자 로그인 아이디 반환
     *
     * @return 사용자 로그인 아이디 (username)
     */
    @Override
    public String getUsername() {
        return username;
    }

    /**
     * 사용자 비밀번호 반환
     *
     * @return 사용자 비밀번호 (password)
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * 사용자가 가진 권한 목록 반환
     *
     * @return 권한 목록 (authorities)
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
}
