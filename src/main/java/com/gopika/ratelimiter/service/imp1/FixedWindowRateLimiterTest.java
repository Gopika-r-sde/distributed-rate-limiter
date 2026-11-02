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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FixedWindowRateLimiterTest {

    private LettuceConnectionFactory connectionFactory;
    private RedisTemplate<String, Object> redisTemplate;
    private FixedWindowRateLimiter rateLimiter;
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

        rateLimiter = new FixedWindowRateLimiter(new RedisScriptExecutor(redisTemplate));

        clientId = "test_fw_" + UUID.randomUUID();
        otherClientId = "test_fw_other_" + UUID.randomUUID();
    }

    @AfterEach
    void tearDown() {
        redisTemplate.delete("ratelimit:fixed_window:" + clientId);
        redisTemplate.delete("ratelimit:fixed_window:" + otherClientId);
        connectionFactory.destroy();
    }

    @Test
    void allowsRequestsUpToTheLimit() {
        ClientConfig config = new ClientConfig(clientId, 3, 60, "FIXED_WINDOW");

        for (int i = 1; i <= 3; i++) {
            assertTrue(rateLimiter.isAllowed(clientId, config), "Request " + i + " should be allowed");
        }
    }

    @Test
    void deniesRequestsAfterTheLimitIsReached() {
        ClientConfig config = new ClientConfig(clientId, 3, 60, "FIXED_WINDOW");

        for (int i = 1; i <= 3; i++) {
            rateLimiter.isAllowed(clientId, config);
        }

        assertFalse(rateLimiter.isAllowed(clientId, config), "Request 4 should be denied");
        assertFalse(rateLimiter.isAllowed(clientId, config), "Request 5 should still be denied");
    }

    @Test
    void keepsSeparateCountersForDifferentClients() {
        ClientConfig config = new ClientConfig(clientId, 2, 60, "FIXED_WINDOW");
        ClientConfig otherConfig = new ClientConfig(otherClientId, 2, 60, "FIXED_WINDOW");

        rateLimiter.isAllowed(clientId, config);
        rateLimiter.isAllowed(clientId, config);
        assertFalse(rateLimiter.isAllowed(clientId, config), "First client should be blocked");

        assertTrue(rateLimiter.isAllowed(otherClientId, otherConfig), "Second client should be unaffected");
    }

    @Test
    void allowsRequestsAgainAfterTheWindowExpires() throws InterruptedException {
        ClientConfig config = new ClientConfig(clientId, 2, 2, "FIXED_WINDOW");

        rateLimiter.isAllowed(clientId, config);
        rateLimiter.isAllowed(clientId, config);
        assertFalse(rateLimiter.isAllowed(clientId, config), "Limit reached, should be denied");

        Thread.sleep(2500);

        assertTrue(rateLimiter.isAllowed(clientId, config), "New window started, should be allowed again");
    }
}