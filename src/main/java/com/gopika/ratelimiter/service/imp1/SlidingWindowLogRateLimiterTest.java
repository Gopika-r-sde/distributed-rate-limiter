package com.gopika.ratelimiter.service.imp1;

import com.gopika.ratelimiter.model.entity.ClientConfig;
import com.gopika.ratelimiter.redis.RedisScriptExecutor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SlidingWindowLogRateLimiterTest {

    private LettuceConnectionFactory connectionFactory;
    private RedisTemplate<String, Object> redisTemplate;
    private SlidingWindowLogRateLimiter rateLimiter;
    private String clientId;
    private String otherClientId;

    @BeforeEach
    void setUp() {
        connectionFactory = new LettuceConnectionFactory("localhost", 6379);
        connectionFactory.afterPropertiesSet();

        redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.afterPropertiesSet();

        rateLimiter = new SlidingWindowLogRateLimiter(new RedisScriptExecutor(redisTemplate));

        clientId = "test_swl_" + UUID.randomUUID();
        otherClientId = "test_swl_other_" + UUID.randomUUID();
    }

    @AfterEach
    void tearDown() {
        redisTemplate.delete("ratelimit:sliding_window_log:" + clientId);
        redisTemplate.delete("ratelimit:sliding_window_log:" + otherClientId);
        connectionFactory.destroy();
    }

    @Test
    void allowsRequestsUpToTheLimit() {
        ClientConfig config = new ClientConfig(clientId, 3, 60, "SLIDING_WINDOW_LOG");

        for (int i = 1; i <= 3; i++) {
            assertTrue(rateLimiter.isAllowed(clientId, config), "Request " + i + " should be allowed");
        }
    }

    @Test
    void deniesRequestsAfterTheLimitIsReached() {
        ClientConfig config = new ClientConfig(clientId, 3, 60, "SLIDING_WINDOW_LOG");

        for (int i = 1; i <= 3; i++) {
            rateLimiter.isAllowed(clientId, config);
        }

        assertFalse(rateLimiter.isAllowed(clientId, config), "Request 4 should be denied");
        assertFalse(rateLimiter.isAllowed(clientId, config), "Request 5 should still be denied");
    }

    @Test
    void keepsSeparateLogsForDifferentClients() {
        ClientConfig config = new ClientConfig(clientId, 2, 60, "SLIDING_WINDOW_LOG");
        ClientConfig otherConfig = new ClientConfig(otherClientId, 2, 60, "SLIDING_WINDOW_LOG");

        rateLimiter.isAllowed(clientId, config);
        rateLimiter.isAllowed(clientId, config);
        assertFalse(rateLimiter.isAllowed(clientId, config), "First client should be blocked");

        assertTrue(rateLimiter.isAllowed(otherClientId, otherConfig), "Second client should be unaffected");
    }

    @Test
    void windowSlidesAsOldRequestsAgeOut() throws InterruptedException {
        ClientConfig config = new ClientConfig(clientId, 2, 3, "SLIDING_WINDOW_LOG");

        assertTrue(rateLimiter.isAllowed(clientId, config), "Request A at t=0 should be allowed");

        Thread.sleep(1500);

        assertTrue(rateLimiter.isAllowed(clientId, config), "Request B at t=1.5s should be allowed");
        assertFalse(rateLimiter.isAllowed(clientId, config), "Request C: window holds A and B, should be denied");

        Thread.sleep(2000);

        assertTrue(rateLimiter.isAllowed(clientId, config), "Request D at t=3.5s: A has aged out, one slot is free");
        assertFalse(rateLimiter.isAllowed(clientId, config), "Request E: window holds B and D, should be denied");
    }

    @Test
    void storesOneEntryPerAllowedRequestOnly() {
        ClientConfig config = new ClientConfig(clientId, 3, 60, "SLIDING_WINDOW_LOG");

        for (int i = 1; i <= 5; i++) {
            rateLimiter.isAllowed(clientId, config);
        }

        Long entries = redisTemplate.opsForZSet().size("ratelimit:sliding_window_log:" + clientId);
        assertEquals(3L, entries, "Only the 3 allowed requests should be logged, not the 2 denied ones");
    }
}