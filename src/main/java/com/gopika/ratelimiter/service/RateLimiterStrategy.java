package com.gopika.ratelimiter.service;

import com.gopika.ratelimiter.model.entity.ClientConfig;

public interface RateLimiterStrategy {

    boolean isAllowed(String clientId, ClientConfig config);

}
