package ru.mafteroid.events;


import java.time.Instant;
import java.util.UUID;

public record CheckRequestedEvent(
        UUID checkId,
        UUID monitorId,
        String url,
        Integer timeoutMs,
        Instant requestedAt
) {}