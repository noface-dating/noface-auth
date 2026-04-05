package com.duri.duriauth.common.config;

import com.duri.duriauth.filter.JwtExceptionFilter;
import com.duri.duriauth.security.filter.JsonUserLoginFilter;
import com.duri.duriauth.security.handler.UserLoginFailureHandler;
import com.duri.duriauth.security.handler.UserLoginSuccessHandler;
import com.duri.duriauth.security.handler.UserLogoutSuccessHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@RequiredArgsConstructor
@Configuration
public class SecurityConfig {

    private final AuthenticationConfiguration authenticationConfiguration;
    private final ObjectMapper objectMapper;
    private final UserLoginSuccessHandler userLoginSuccessHandler;
    private final UserLoginFailureHandler userLoginFailureHandler;
    private final UserLogoutSuccessHandler userLogoutSuccessHandler;

    private final JwtExceptionFilter jwtExceptionFilter;

    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // TODO: Password Encoder 적용
        return NoOpPasswordEncoder.getInstance();   // 테스트 위해 Encoder 비활성화
//        return new BCryptPasswordEncoder();
    }

    @Bean
    public JsonUserLoginFilter jsonLoginFilter(AuthenticationManager authenticationManager) {
        JsonUserLoginFilter loginFilter = new JsonUserLoginFilter(objectMapper);
        loginFilter.setAuthenticationManager(authenticationManager);

        // 로그인 URL
        loginFilter.setFilterProcessesUrl("/auth/login");

        // 핸들러 연결
        loginFilter.setAuthenticationSuccessHandler(userLoginSuccessHandler);
        loginFilter.setAuthenticationFailureHandler(userLoginFailureHandler);

        return loginFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JsonUserLoginFilter jsonUserLoginFilter)
            throws Exception
    {
        http
                // CorsFilter가 Security 체인 최앞에서 OPTIONS 프리플라이트를 처리
                // → JsonUserLoginFilter까지 도달하지 않아 302 리다이렉트 방지
                .cors(Customizer.withDefaults())

                // Spring Security 기본 설정
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                // URL 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()
                        .anyRequest().authenticated()
                )

                // 로그인 필터
                .addFilterAt(jsonUserLoginFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtExceptionFilter, JsonUserLoginFilter.class)

                // 로그아웃 설정
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .logoutSuccessHandler(userLogoutSuccessHandler)
                );

        return http.build();
    }
}
