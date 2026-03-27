package com.duri.duriauth.controller;

import com.duri.duriauth.domain.TokenPair;
import com.duri.duriauth.dto.request.LoginRequestDto;
import com.duri.duriauth.service.AuthService;
import com.duri.duriauth.web.cookie.CookieService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/auth")
@RequiredArgsConstructor
@RestController
public class AuthController {

    // Spring Security Config TEST API
    @GetMapping("/test")
    public String authTest() {
        return "공개 API 접속 : /auth/** 테스트 성공";
    }

}
