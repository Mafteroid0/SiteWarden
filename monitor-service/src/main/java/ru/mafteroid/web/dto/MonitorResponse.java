package ru.mafteroid.web.dto;

import ru.mafteroid.entity.Monitor;

import java.time.Instant;
import java.util.UUID;

public record MonitorResponse(
        UUID id,
        String name,
        String url,
        Integer intervalSeconds,
        Integer timeoutMs,
        boolean enabled,
        Instant nextCheckAt,
        Instant createdAt
) {
    public MonitorResponse(Monitor monitor){
        this(
                monitor.getId(),
                monitor.getName(),
                monitor.getUrl(),
                monitor.getIntervalSeconds(),
                monitor.getTimeoutMs(),
                monitor.getEnabled(),
                monitor.getNextCheckAt(),
                monitor.getCreatedAt()
        );
    }
}