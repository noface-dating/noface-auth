package com.duri.duriauth.common.config;

import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@RequiredArgsConstructor
@Configuration
public class CorsConfig {

    @Value("${cors.allowed-origins}")
    private String allowedOriginsProperty;

    // Front 서버 --로그인 및 로그아웃 요청--> Auth 서버 가능하게 하기 위한 CORS 설정
    // WebMvcConfigurer.addCorsMappings 는 DispatcherServlet 레벨 동작 → Spring Security 필터 체인에 적용되지 않음
    // CorsConfigurationSource 빈 + SecurityConfig의 http.cors() 조합 필요

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        // properties 값 → List 변환 (콤마 분리 지원)
        List<String> allowedOrigins = Arrays.stream(
                        StringUtils.commaDelimitedListToStringArray(allowedOriginsProperty)
                )
                .map(String::trim)
                .toList();

        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
