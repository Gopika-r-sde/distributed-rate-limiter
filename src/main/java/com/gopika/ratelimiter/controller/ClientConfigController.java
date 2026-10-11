package com.gopika.ratelimiter.controller;


import com.gopika.ratelimiter.model.dto.ClientConfigRequest;
import com.gopika.ratelimiter.model.entity.ClientConfig;
import com.gopika.ratelimiter.model.entity.RequestLog;
import com.gopika.ratelimiter.repository.ClientConfigRepository;
import com.gopika.ratelimiter.repository.RequestLogRepository;
import com.gopika.ratelimiter.service.RateLimiterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
public class ClientConfigController {

    private final ClientConfigRepository clientConfigRepository;
    private final RequestLogRepository requestLogRepository;
    public final RateLimiterFactory rateLimiterFactory;

    public ClientConfigController(ClientConfigRepository clientConfigRepository, RequestLogRepository requestLogRepository, RateLimiterFactory rateLimiterFactory) {
        this.clientConfigRepository = clientConfigRepository;
        this.requestLogRepository = requestLogRepository;
        this.rateLimiterFactory = rateLimiterFactory;
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public ClientConfig createClient(@RequestBody ClientConfigRequest request) {
        if(request.getClientId() == null || request.getClientId().isBlank()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ClientId is required");
        }
        validateRules(request);

        if(clientConfigRepository.findByClientId(request.getClientId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Client already exists: " + request.getClientId());
        }

        ClientConfig config = new ClientConfig(
                request.getClientId(),
                request.getMaxRequests(),
                request.getWindowSizeInSeconds(),
                request.getAlgorithmType());

        return clientConfigRepository.save(config);
    }

    @GetMapping
    public List<ClientConfig> getAllClients() {
        return clientConfigRepository.findAll();
    }

    @PutMapping("/{clientId}")
    public ClientConfig updateClient(@PathVariable String clientId, @RequestBody ClientConfigRequest request) {

        ClientConfig config = clientConfigRepository.findByClientId(clientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client not found: " + clientId));

        validateRules(request);

        config.setMaxRequests(request.getMaxRequests());
        config.setWindowSizeInSeconds(request.getWindowSizeInSeconds());
        config.setAlgorithmType(request.getAlgorithmType());

        return clientConfigRepository.save(config);
    }

    @GetMapping("/{clientId}/logs")
    public List<RequestLog> getClientLogs(@PathVariable String clientId) {
        return requestLogRepository.findByClientId(clientId);
    }

    private void validateRules(ClientConfigRequest request) {
        if(request.getMaxRequests() <= 0 || request.getWindowSizeInSeconds() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Max Requests and/or Window Size must be greater than 0");
        }
        try {
            rateLimiterFactory.getStrategy(request.getAlgorithmType());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}
