package com.gopika.ratelimiter.service.imp1;

import com.gopika.ratelimiter.model.entity.ClientConfig;
import com.gopika.ratelimiter.redis.RedisScriptExecutor;
import com.gopika.ratelimiter.service.RateLimiterStrategy;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("TOKEN_BUCKET")
public class TokenBucketRateLimiter implements RateLimiterStrategy {

    private final RedisScriptExecutor redisScriptExecutor;

    public TokenBucketRateLimiter(RedisScriptExecutor redisScriptExecutor) {
        this.redisScriptExecutor = redisScriptExecutor;
    }

    @Override
    public boolean isAllowed(String clientId, ClientConfig config) {
        String key = "ratelimit:token_bucket:" + clientId;

        double refillRatePerSecond = (double) config.getMaxRequests() / config.getWindowSizeInSeconds();

        Long result = redisScriptExecutor.executeScript(
                "scripts/token_bucket.lua",
                key,
                List.of(
                      String.valueOf(config.getMaxRequests()),
                      String.valueOf(refillRatePerSecond)
                )
        );

        return result != null && result == 1L;
    }

}
