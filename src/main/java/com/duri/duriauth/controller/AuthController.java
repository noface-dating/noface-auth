package com.duri.duriauth.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/* Spring Security Setting TEST Controller */
@RestController
public class AuthController {

    @GetMapping("/actuator/test")
    public String actuatorTest() {
        return "공개 API 접속 : /actuator/** 테스트 성공";
    }

    @GetMapping("/health")
    public String health() {
        return "공개 API 접속 : /health 테스트 성공";
    }

    @GetMapping("/auth/test")
    public String authTest() {
        return "공개 API 접속 : /auth/** 테스트 성공";
    }

}
