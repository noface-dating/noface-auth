package com.duri.duriauth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuthRedisService {

    private static final String REFRESH_BLACKLIST_PREFIX = "auth:refresh:blacklist:";

    private final RedisTemplate<String, String> redisTemplate;

    private String key(String prefix, String id) {
        return prefix + id;
    }

    private void saveRefreshBlacklist(String refreshJti) {
        String key = key(REFRESH_BLACKLIST_PREFIX, refreshJti);
    }

}
