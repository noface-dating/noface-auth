package com.duri.duriauth.security.principal;

/**
 * 인증된 사용자 정보를 추상화한 인터페이스
 *
 * <p>
 *     - 주로 로그인된 사용자의 식별자(User ID)를 제공하는 용도로 사용
 * </p>
 */
public interface AuthPrincipal {

    /**
     * 인증된 사용자의 고유 식별자 반환
     * @return 사용자 고유 식별자 (User ID)
     */
    String getUserId();
}
