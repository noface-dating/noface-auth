package com.duri.duriauth.common.properties;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String issuer;
    private String publicKey;
    private String privateKey;
    private AccessToken access = new AccessToken();
    private RefreshToken refresh = new RefreshToken();

    @Setter
    @Getter
    public static class AccessToken {
        private Duration validity;
    }

    @Setter
    @Getter
    public static class RefreshToken {
        private Duration validity;
    }

}
