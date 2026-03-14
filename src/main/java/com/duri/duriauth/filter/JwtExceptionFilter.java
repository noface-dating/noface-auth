package com.duri.duriauth.filter;

import com.duri.duriauth.dto.response.ErrorResponse;
import com.duri.duriauth.exception.AuthErrorCode;
import com.duri.duriauth.exception.AuthException;
import com.duri.duriauth.exception.BaseErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
@Component
public class JwtExceptionFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException
    {
        try {
            filterChain.doFilter(request, response);
        } catch (AuthException e) {
            this.handleError(response, e.getErrorCode());
        } catch (ExpiredJwtException e) {
            this.handleError(response, AuthErrorCode.EXPIRED_TOKEN);
        } catch (JwtException e) {
            this.handleError(response, AuthErrorCode.INVALID_TOKEN);
        }
    }

    private void handleError(HttpServletResponse response,
                             BaseErrorCode errorCode) throws IOException
    {
        // 이미 응답이 전송된 경우, 추가 응답 작성X
        if (response.isCommitted()) {
            return;
        }

        response.resetBuffer();
        response.setStatus(errorCode.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ErrorResponse errorResponse = ErrorResponse.from(errorCode);
        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}
