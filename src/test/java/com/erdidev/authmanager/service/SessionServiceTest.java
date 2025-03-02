package com.erdidev.authmanager.service;

import com.erdidev.authmanager.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.InjectMocks;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SessionServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private SessionService sessionService;

    private User testUser;
    private String sessionId;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        
        sessionId = UUID.randomUUID().toString();
        
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void testCreateSession() {
        String sessionKey = "test-session";
        
        sessionService.createSession(sessionKey, testUser);
        
        verify(redisTemplate).opsForValue();
        verify(valueOperations).set(
            eq("session:" + sessionKey),
            eq(testUser),
            eq(24L),
            eq(TimeUnit.HOURS)
        );
    }

    @Test
    void testGetSession() {
        when(valueOperations.get("session:" + sessionId)).thenReturn(testUser);
        
        User result = sessionService.getSession(sessionId);
        
        verify(redisTemplate).opsForValue();
        verify(valueOperations).get("session:" + sessionId);
        assertEquals(testUser, result);
    }

    @Test
    void testInvalidateSession() {
        sessionService.invalidateSession(sessionId);
        
        verify(redisTemplate).delete("session:" + sessionId);
    }

    @Test
    void testGetExpiredSession() {
        when(valueOperations.get("session:" + sessionId)).thenReturn(null);
        
        User result = sessionService.getSession(sessionId);
        
        verify(redisTemplate).opsForValue();
        verify(valueOperations).get("session:" + sessionId);
        assertNull(result);
    }
} 