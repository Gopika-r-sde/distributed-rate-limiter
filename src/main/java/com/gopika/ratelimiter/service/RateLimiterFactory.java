package com.gopika.ratelimiter.service;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RateLimiterFactory {

    private final Map<String, RateLimiterStrategy> strategies;

    public RateLimiterFactory(Map<String, RateLimiterStrategy> strategies) {
        this.strategies = strategies;
    }

    public RateLimiterStrategy getStrategy(String algorithmType) {
        RateLimiterStrategy strategy = strategies.get(algorithmType);

        if (strategy == null) {
            throw new IllegalArgumentException("Unknown algorithm type: " + algorithmType);
        }

        return strategy;
    }

}
