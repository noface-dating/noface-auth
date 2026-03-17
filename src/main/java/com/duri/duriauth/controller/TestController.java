package com.duri.duriauth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api/test")
    public ResponseEntity<String> test(Authentication authentication) {
        String userId = authentication.getPrincipal().toString();
        return ResponseEntity.ok("보호 API 접근 성공 : " + userId);
    }
}
