package com.duri.duriauth.domain;

public record TokenPair(
        String accessToken,
        String refreshToken
) {
}
