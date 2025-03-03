package com.erdidev.authmanager.service;

import com.erdidev.authmanager.model.User;
import com.erdidev.authmanager.security.SessionUser;
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
        SessionUser sessionUser = SessionUser.fromUser(user);
        redisTemplate.opsForValue().set(key, sessionUser, SESSION_DURATION, TimeUnit.HOURS);
    }

    public User getSession(String sessionId) {
        String key = SESSION_PREFIX + sessionId;
        SessionUser sessionUser = (SessionUser) redisTemplate.opsForValue().get(key);
        return sessionUser != null ? sessionUser.toUser() : null;
    }

    public void invalidateSession(String sessionId) {
        String key = SESSION_PREFIX + sessionId;
        redisTemplate.delete(key);
    }

    public void refreshSession(String sessionId) {
        String key = SESSION_PREFIX + sessionId;
        if (redisTemplate.hasKey(key)) {
            redisTemplate.expire(key, SESSION_DURATION, TimeUnit.HOURS);
        }
    }
} 