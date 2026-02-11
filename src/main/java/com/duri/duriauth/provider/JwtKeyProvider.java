package com.duri.duriauth.provider;

import jakarta.annotation.PostConstruct;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import lombok.Getter;
import org.springframework.stereotype.Component;

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
            throw new IllegalStateException("EC Key 생성 실패", e);
        }
    }
}
