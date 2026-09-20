package com.gopika.ratelimiter.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "client_config")
public class ClientConfig {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, unique = true)
    private String clientId;

    @Column(nullable = false)
    private int maxRequests;

    @Column(nullable = false)
    private int windowSizeInSeconds;

    @Column(nullable = false)
    private String algorithmType;

    // Constructors
    public ClientConfig() {}

    public ClientConfig(String clientId, int maxRequests, int windowSizeInSeconds, String algorithmType) {
        this.clientId = clientId;
        this.maxRequests = maxRequests;
        this.windowSizeInSeconds = windowSizeInSeconds;
        this.algorithmType = algorithmType;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public int getMaxRequests() {
        return maxRequests;
    }

    public void setMaxRequests(int maxRequests) {
        this.maxRequests = maxRequests;
    }

    public int getWindowSizeInSeconds() {
        return windowSizeInSeconds;
    }

    public void setWindowSizeInSeconds(int windowSizeInSeconds) {
        this.windowSizeInSeconds = windowSizeInSeconds;
    }

    public String getAlgorithmType() {
        return algorithmType;
    }

    public void setAlgorithmType(String algorithmType) {
        this.algorithmType = algorithmType;
    }
}
