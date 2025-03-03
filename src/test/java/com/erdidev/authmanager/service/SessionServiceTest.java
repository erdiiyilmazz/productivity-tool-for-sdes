package com.erdidev.authmanager.service;

import com.erdidev.authmanager.model.User;
import com.erdidev.authmanager.security.SessionUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.InjectMocks;
import org.mockito.ArgumentMatcher;

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
    private SessionUser sessionUser;
    private String sessionId;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        
        sessionUser = SessionUser.fromUser(testUser);
        sessionId = UUID.randomUUID().toString();
        
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    private static ArgumentMatcher<SessionUser> sessionUserMatcher(SessionUser expected) {
        return actual -> actual != null && 
                        actual.getUsername().equals(expected.getUsername()) &&
                        actual.getId().equals(expected.getId());
    }

    @Test
    void testCreateSession() {
        String sessionKey = "test-session";
        
        sessionService.createSession(sessionKey, testUser);
        
        verify(redisTemplate).opsForValue();
        verify(valueOperations).set(
            eq("session:" + sessionKey),
            argThat(sessionUserMatcher(sessionUser)),
            eq(24L),
            eq(TimeUnit.HOURS)
        );
    }

    @Test
    void testGetSession() {
        when(valueOperations.get("session:" + sessionId)).thenReturn(sessionUser);
        
        User result = sessionService.getSession(sessionId);
        
        verify(redisTemplate).opsForValue();
        verify(valueOperations).get("session:" + sessionId);
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getUsername(), result.getUsername());
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