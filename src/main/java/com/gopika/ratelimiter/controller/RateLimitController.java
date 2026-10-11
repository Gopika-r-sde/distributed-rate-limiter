package com.gopika.ratelimiter.controller;


import com.gopika.ratelimiter.exception.RateLimitExceededException;
import com.gopika.ratelimiter.model.dto.RateLimitRequest;
import com.gopika.ratelimiter.model.dto.RateLimitResponse;
import com.gopika.ratelimiter.model.entity.ClientConfig;
import com.gopika.ratelimiter.model.entity.RequestLog;
import com.gopika.ratelimiter.repository.ClientConfigRepository;
import com.gopika.ratelimiter.repository.RequestLogRepository;
import com.gopika.ratelimiter.service.RateLimiterFactory;
import com.gopika.ratelimiter.service.RateLimiterStrategy;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@RestController
@RequestMapping("/api/ratelimit")
public class RateLimitController {

    private final ClientConfigRepository clientConfigRepository;
    private final RequestLogRepository requestLogRepository;
    private final RateLimiterFactory rateLimiterFactory;

    public RateLimitController(ClientConfigRepository clientConfigRepository, RequestLogRepository requestLogRepository, RateLimiterFactory rateLimiterFactory) {
        this.clientConfigRepository = clientConfigRepository;
        this.requestLogRepository = requestLogRepository;
        this.rateLimiterFactory = rateLimiterFactory;
    }

    @PostMapping("/check")
    public RateLimitResponse checkRateLimit(@RequestBody RateLimitRequest request) {
        String clientId = request.getClientId();

        ClientConfig config = clientConfigRepository.findByClientId(clientId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No config found for client: " + clientId
                ));

        RateLimiterStrategy strategy = rateLimiterFactory.getStrategy(config.getAlgorithmType());
        boolean allowed = strategy.isAllowed(clientId, config);

        requestLogRepository.save(new RequestLog(clientId, Instant.now(), allowed, config.getAlgorithmType()));

        if(!allowed) {
            throw new RateLimitExceededException("Rate limit exceeded for client: " + clientId);
        }

        return new RateLimitResponse(true, -1, "Request allowed");
    }

}
