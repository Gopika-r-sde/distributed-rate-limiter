package com.gopika.ratelimiter.repository;

import com.gopika.ratelimiter.model.entity.ClientConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClientConfigRepository extends JpaRepository<ClientConfig, Long> {

    Optional<ClientConfig> findByClientId(String clientId);
}
