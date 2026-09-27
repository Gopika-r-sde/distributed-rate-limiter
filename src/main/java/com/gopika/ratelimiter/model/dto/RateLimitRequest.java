package com.gopika.ratelimiter.model.dto;

public class RateLimitRequest {

    private String clientId;

    public RateLimitRequest() {

    }

    public RateLimitRequest(String clientId) {
        this.clientId = clientId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}
