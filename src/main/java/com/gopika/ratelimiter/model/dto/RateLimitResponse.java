package com.gopika.ratelimiter.model.dto;

public class RateLimitResponse {

    private boolean allowed;
    private int remainingRequests;
    private String message;

    public RateLimitResponse() {

    }

    public RateLimitResponse(boolean allowed, int remainingRequests, String message) {
        this.allowed = allowed;
        this.remainingRequests = remainingRequests;
        this.message = message;
    }

    public boolean isAllowed() {
        return allowed;
    }

    public void setAllowed(boolean allowed) {
        this.allowed = allowed;
    }

    public int getRemainingRequests() {
        return remainingRequests;
    }

    public void setRemainingRequests(int remainingRequests) {
        this.remainingRequests = remainingRequests;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
