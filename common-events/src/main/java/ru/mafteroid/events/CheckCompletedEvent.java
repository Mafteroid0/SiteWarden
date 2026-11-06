package ru.mafteroid.events;

import java.time.Instant;
import java.util.UUID;

public record CheckCompletedEvent(
        UUID checkId,
        UUID monitorId,
        CheckType checkType,
        Instant startedAt,
        Instant completedAt,
        boolean successful,
        Integer httpStatus,
        long durationMs,
        String errorCode
) {
}
