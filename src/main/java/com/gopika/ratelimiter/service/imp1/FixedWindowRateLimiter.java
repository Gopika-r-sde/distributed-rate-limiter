package com.gopika.ratelimiter.service.imp1;

import com.gopika.ratelimiter.model.entity.ClientConfig;
import com.gopika.ratelimiter.redis.RedisScriptExecutor;
import com.gopika.ratelimiter.service.RateLimiterStrategy;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("FIXED_WINDOW")
public class FixedWindowRateLimiter implements RateLimiterStrategy {

    private final RedisScriptExecutor redisScriptExecutor;

    public FixedWindowRateLimiter(RedisScriptExecutor redisScriptExecutor) {
        this.redisScriptExecutor = redisScriptExecutor;
    }

    @Override
    public boolean isAllowed(String clientId, ClientConfig config) {
        String key = "ratelimit:fixed_window:" + clientId;

        Long result = redisScriptExecutor.executeScript(
                "scripts/fixed_window.lua",
                key,
                List.of(
                       String.valueOf(config.getMaxRequests()),
                       String.valueOf(config.getWindowSizeInSeconds())
                )
        );
        return result != null && result == 1L;
    }
}
