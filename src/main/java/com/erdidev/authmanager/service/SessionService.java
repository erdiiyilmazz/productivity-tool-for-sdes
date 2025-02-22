package com.erdidev.authmanager.service;

import com.erdidev.authmanager.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class SessionService {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String SESSION_PREFIX = "session:";
    private static final long SESSION_DURATION = 24; // hours

    public void createSession(String sessionId, User user) {
        String key = SESSION_PREFIX + sessionId;
        redisTemplate.opsForValue().set(key, user, SESSION_DURATION, TimeUnit.HOURS);
    }

    public User getSession(String sessionId) {
        String key = SESSION_PREFIX + sessionId;
        return (User) redisTemplate.opsForValue().get(key);
    }

    public void invalidateSession(String sessionId) {
        String key = SESSION_PREFIX + sessionId;
        redisTemplate.delete(key);
    }
} 