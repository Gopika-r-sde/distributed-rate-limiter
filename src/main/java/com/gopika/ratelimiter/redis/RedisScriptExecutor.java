package com.gopika.ratelimiter.redis;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class RedisScriptExecutor {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisScriptExecutor(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Long executeScript(String scriptPath, String key, List<String> args) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setLocation((new ClassPathResource(scriptPath)));
        script.setResultType(Long.class);

        return redisTemplate.execute(script, Collections.singletonList(key), args.toArray());
    }
}
