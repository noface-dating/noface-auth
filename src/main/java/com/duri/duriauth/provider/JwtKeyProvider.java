package com.duri.duriauth.provider;

import com.duri.duriauth.common.properties.JwtProperties;
import com.duri.duriauth.exception.logging.JwtKeyInitializationException;
import jakarta.annotation.PostConstruct;
import java.security.KeyFactory;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Objects;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * JWT 서명에 사용되는 EC KeyPair를 제공하는 클래스
 *
 * <p>
 *     PEM 파일 기반의 고정 256 bits EC KeyPair를 사용하며, JWT Token 서명 및 검증에 사용된다.
 * </p>
 *
 */

@Getter
@RequiredArgsConstructor
@Component
public class JwtKeyProvider {

    private final JwtProperties jwtProperties;

    private ECPrivateKey privateKey;
    private ECPublicKey publicKey;

    @PostConstruct
    public void init() {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("EC");

            // Public Key
            byte[] publicBytes = Base64.getDecoder()
                    .decode(this.normalize(jwtProperties.getPublicKey()));
            X509EncodedKeySpec publicSpec = new X509EncodedKeySpec(publicBytes);
            this.publicKey = (ECPublicKey) keyFactory.generatePublic(publicSpec);

            // Private Key
            if (Objects.nonNull(jwtProperties.getPrivateKey()) && !jwtProperties.getPrivateKey().isBlank()) {
                byte[] privateBytes = Base64.getDecoder()
                        .decode(this.normalize(jwtProperties.getPrivateKey()));
                PKCS8EncodedKeySpec privateSpec = new PKCS8EncodedKeySpec(privateBytes);
                this.privateKey = (ECPrivateKey) keyFactory.generatePrivate(privateSpec);
            }

        } catch (Exception e) {
            throw new JwtKeyInitializationException("PEM Key 로딩 실패", e);
        }
    }

    private String normalize(String key) {
        return key.replaceAll("\\s", "");
    }
}
