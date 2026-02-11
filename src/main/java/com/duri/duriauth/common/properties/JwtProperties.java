package com.duri.duriauth.common.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@ConfigurationProperties(prefix = "jwt")
@Component
public class JwtProperties {

    private String issuer;
    private AccessToken access = new AccessToken();
    private RefreshToken refresh = new RefreshToken();

    @Setter
    @Getter
    public static class AccessToken {
        private Long validity;
    }

    @Setter
    @Getter
    public static class RefreshToken {
        private Long validity;
    }

}
