package com.gopika.ratelimiter.repository;

import com.gopika.ratelimiter.model.entity.RequestLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RequestLogRepository extends JpaRepository<RequestLog, Long> {

    List<RequestLog> findByClientId(String clientId);
}
