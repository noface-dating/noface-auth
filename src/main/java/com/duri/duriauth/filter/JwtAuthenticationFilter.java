package com.duri.duriauth.filter;

import static com.duri.duriauth.domain.TokenType.ACCESS;

import com.duri.duriauth.domain.TokenType;
import com.duri.duriauth.entity.UserRole;
import com.duri.duriauth.exception.AuthErrorCode;
import com.duri.duriauth.exception.AuthException;
import com.duri.duriauth.provider.JwtTokenProvider;
import com.duri.duriauth.web.cookie.CookieService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/* TEST 전용 JWT 인증 필터
*  - Front 서버에서 수정해서 사용O
*  - Auth 서버에서 사용하지 않지만, 예비용으로 코드 삭제X
*/

// OncePerRequestFilter 상속 : 요청당 필터 1번만 실행 보장
@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CookieService cookieService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException
    {
        try {
            // 이미 인증된 경우 SKIP
            if (!Objects.isNull(SecurityContextHolder.getContext().getAuthentication())) {
                filterChain.doFilter(request, response);
                return;
            }

            Optional<String> optionalAccessToken = cookieService.getAccessToken(request);
            if (optionalAccessToken.isEmpty()) {
                // 토큰이 존재하지 않는 경우, 인증 시도X (인증이 필요없는 정상적인 요청일 수 있음)
                filterChain.doFilter(request, response);
                return;
            }

            String accessToken = optionalAccessToken.get();

            // 토큰 서명 + issuer + 만료시간 검증 --> Claims 파싱 수행
            Claims claims = jwtTokenProvider.parseClaims(accessToken);

            // 토큰 타입 검증
            TokenType tokenType = jwtTokenProvider.getTokenType(claims);
            if (tokenType != ACCESS) {
                throw new AuthException(AuthErrorCode.INVALID_TOKEN);
            }

            // 인증 정보 추출
            String userId = jwtTokenProvider.getUserId(claims);

            UserRole userRole = jwtTokenProvider.getRole(claims);
            List<GrantedAuthority> authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_" + userRole.name())
            );

            // 인증 정보 저장
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userId,
                    null,
                    authorities
            );

            if (Objects.isNull(SecurityContextHolder.getContext().getAuthentication())) {
                // 인증 정보가 존재하지 않는 경우에만 새로운 인증 정보 저장
                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(authentication);
                SecurityContextHolder.setContext(context);
            }

            filterChain.doFilter(request, response);

        } finally {
            // ThreadLocal 정리
            SecurityContextHolder.clearContext();
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return path.startsWith("/auth/login");
    }
}
