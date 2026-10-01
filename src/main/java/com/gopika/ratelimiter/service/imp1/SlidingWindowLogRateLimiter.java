package com.gopika.ratelimiter.service.imp1;

import com.gopika.ratelimiter.model.entity.ClientConfig;
import com.gopika.ratelimiter.redis.RedisScriptExecutor;
import com.gopika.ratelimiter.service.RateLimiterStrategy;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("SLIDING_WINDOW_LOG")
public class SlidingWindowLogRateLimiter implements RateLimiterStrategy {

    private final RedisScriptExecutor redisScriptExecutor;

    public SlidingWindowLogRateLimiter(RedisScriptExecutor redisScriptExecutor) {
        this.redisScriptExecutor = redisScriptExecutor;
    }

    @Override
    public boolean isAllowed(String clientId, ClientConfig config) {
        String key = "ratelimit:sliding_window_log:" + clientId;

        Long result = redisScriptExecutor.executeScript(
                "scripts/sliding_window_log.lua",
                key,
                List.of(
                        String.valueOf(config.getMaxRequests()),
                        String.valueOf(config.getWindowSizeInSeconds())
                )
        );

        return result != null && result == 1L;
    }
}
