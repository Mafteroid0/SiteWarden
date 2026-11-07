package ru.mafteroid;

import ru.mafteroid.entity.MonitorStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MonitorStatusResponse(
        UUID MonitorId,
        String status,
        OffsetDateTime lastCheckedAt
) {
}
