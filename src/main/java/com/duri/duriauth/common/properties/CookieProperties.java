package com.duri.duriauth.common.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "web.cookie")
public class CookieProperties {

    private String domain;
    private String accessPath;
    private String refreshPath;
    private boolean httpOnly;
    private boolean secure;
    private String sameSite;
    private int accessMaxAge;
    private int refreshMaxAge;

}
