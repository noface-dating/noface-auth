package com.duri.duriauth.provider;

import com.duri.duriauth.exception.logging.JwtKeyInitializationException;
import jakarta.annotation.PostConstruct;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import lombok.Getter;
import org.springframework.stereotype.Component;

/**
 * JWT 서명에 사용되는 EC KeyPair를 제공하는 클래스
 *
 * <p>
 *     애플리케이션 시작 시 256 bits EC KeyPair를 생성하며, JWT Token 서명 및 검증에 사용된다.
 * </p>
 *
 * <p>
 *     현재는 개발 환경용으로 Key를 메모리에서 동적으로 생성하며,
 *     운영 환경에서는 PEM 파일 기반의 고정 키 로딩 방식으로 전환 예정이다.
 * </p>
 */

@Getter
@Component
public class JwtKeyProvider {

    // TODO: 운영 환경에서는 PEM 파일 기반 키 로딩 방식으로 전환

    private ECPrivateKey privateKey;
    private ECPublicKey publicKey;

    @PostConstruct
    public void init() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
            keyPairGenerator.initialize(256);

            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            this.privateKey = (ECPrivateKey) keyPair.getPrivate();
            this.publicKey = (ECPublicKey) keyPair.getPublic();

        } catch (Exception e) {
            throw new JwtKeyInitializationException("EC Key 생성 실패", e);
        }
    }
}
