package com.duri.duriauth.security.filter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * JSON 형식의 로그인 요청을 처리하는 Spring Security 인증 필터
 *
 * <p>
 *     - 요청 Body에서 username, password를 추출하여 AuthenticationManager에 인증 위임
 * </p>
 */
@RequiredArgsConstructor
public class JsonUserLoginFilter extends UsernamePasswordAuthenticationFilter {

    private final ObjectMapper objectMapper;

    /**
     * JSON 로그인 요청 인증 수행
     *
     * <p>
     *      - 로그인 요청 시 JSON Body를 읽고, Authentication 객체를 생성하여 인증 수행
     *      - Content-Type이 JSON이 아닌 경우, 기본 UsernamePasswordAuthenticationFilter 로직 수행
     * </p>
     *
     * @param request HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @return 인증 성공 시 Authentication 객체 반환
     * @throws AuthenticationException 인증 실패 시 예외 발생
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {

        // 1. JSON 요청만 처리
        if (Objects.nonNull(request.getContentType()) &&
            request.getContentType().startsWith("application/json"))
        {
            try {
                // 2. JSON --> Map 파싱
                Map<String, String> loginData = objectMapper.readValue(
                        request.getInputStream(),
                        new TypeReference<>() {}
                );

                String username = loginData.get("username");
                String password = loginData.get("password");

                // 3. Authentication Token 생성
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(username, password);

                setDetails(request, authToken);

                // 4. AuthenticationManager에 사용자 인증 위임
                //    - DaoAuthenticationProvider
                //      > UsernamePasswordAuthenticationToken
                //        > CustomUserDetails / null / Authorities
                return getAuthenticationManager().authenticate(authToken);

            } catch (IOException e) {
                throw new AuthenticationServiceException("Invalid Login Request", e);
            }
        }

        return super.attemptAuthentication(request, response);
    }
}
