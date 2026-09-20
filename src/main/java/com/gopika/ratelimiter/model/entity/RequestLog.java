package com.gopika.ratelimiter.model.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "request_log")
public class RequestLog {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String clientId;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(nullable = false)
    private boolean allowed;

    @Column(nullable = false)
    private String algorithmType;

    // Constructors
    public RequestLog() {}

    public RequestLog(String clientId, Instant timestamp, boolean allowed, String algorithmType) {
        this.clientId = clientId;
        this.timestamp = timestamp;
        this.allowed = allowed;
        this.algorithmType = algorithmType;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isAllowed() {
        return allowed;
    }

    public void setAllowed(boolean allowed) {
        this.allowed = allowed;
    }

    public String getAlgorithmType() {
        return algorithmType;
    }

    public void setAlgorithmType(String algorithmType) {
        this.algorithmType = algorithmType;
    }
}
