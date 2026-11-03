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

class TokenBucketRateLimiterTest {

    private LettuceConnectionFactory connectionFactory;
    private RedisTemplate<String, Object> redisTemplate;
    private TokenBucketRateLimiter rateLimiter;
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

        rateLimiter = new TokenBucketRateLimiter(new RedisScriptExecutor(redisTemplate));

        clientId = "test_tb_" + UUID.randomUUID();
        otherClientId = "test_tb_other_" + UUID.randomUUID();
    }

    @AfterEach
    void tearDown() {
        redisTemplate.delete("ratelimit:token_bucket:" + clientId);
        redisTemplate.delete("ratelimit:token_bucket:" + otherClientId);
        connectionFactory.destroy();
    }

    @Test
    void allowsRequestsUpToCapacity() {
        ClientConfig config = new ClientConfig(clientId, 3, 600, "TOKEN_BUCKET");

        for (int i = 1; i <= 3; i++) {
            assertTrue(rateLimiter.isAllowed(clientId, config), "Request " + i + " should be allowed");
        }
    }

    @Test
    void deniesRequestsWhenBucketIsEmpty() {
        ClientConfig config = new ClientConfig(clientId, 3, 600, "TOKEN_BUCKET");

        for (int i = 1; i <= 3; i++) {
            rateLimiter.isAllowed(clientId, config);
        }

        assertFalse(rateLimiter.isAllowed(clientId, config), "Request 4 should be denied");
        assertFalse(rateLimiter.isAllowed(clientId, config), "Request 5 should still be denied");
    }

    @Test
    void keepsSeparateBucketsForDifferentClients() {
        ClientConfig config = new ClientConfig(clientId, 2, 600, "TOKEN_BUCKET");
        ClientConfig otherConfig = new ClientConfig(otherClientId, 2, 600, "TOKEN_BUCKET");

        rateLimiter.isAllowed(clientId, config);
        rateLimiter.isAllowed(clientId, config);
        assertFalse(rateLimiter.isAllowed(clientId, config), "First client's bucket should be empty");

        assertTrue(rateLimiter.isAllowed(otherClientId, otherConfig), "Second client's bucket should be full");
    }

    @Test
    void refillsTokensGraduallyOverTime() throws InterruptedException {
        ClientConfig config = new ClientConfig(clientId, 2, 4, "TOKEN_BUCKET");

        rateLimiter.isAllowed(clientId, config);
        rateLimiter.isAllowed(clientId, config);
        assertFalse(rateLimiter.isAllowed(clientId, config), "Bucket is empty, should be denied");

        Thread.sleep(3000);

        assertTrue(rateLimiter.isAllowed(clientId, config), "About 1.5 tokens refilled, should be allowed");
    }

    @Test
    void neverRefillsBeyondCapacity() throws InterruptedException {
        ClientConfig config = new ClientConfig(clientId, 2, 4, "TOKEN_BUCKET");

        rateLimiter.isAllowed(clientId, config);

        Thread.sleep(4000);

        assertTrue(rateLimiter.isAllowed(clientId, config), "Bucket refilled to capacity, first request allowed");
        assertTrue(rateLimiter.isAllowed(clientId, config), "Second request allowed");
        assertFalse(rateLimiter.isAllowed(clientId, config), "Refill is capped at 2, so a third must be denied");
    }
}