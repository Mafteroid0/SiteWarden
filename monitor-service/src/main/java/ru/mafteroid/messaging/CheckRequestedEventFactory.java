package ru.mafteroid.messaging;

import ru.mafteroid.entity.Monitor;
import ru.mafteroid.events.CheckRequestedEvent;

import java.time.Instant;
import java.util.UUID;

public final class CheckRequestedEventFactory {

    private CheckRequestedEventFactory() {
    }

    public static CheckRequestedEvent fromMonitor(Monitor monitor) {
        return new CheckRequestedEvent(
                UUID.randomUUID(),
                monitor.getId(),
                monitor.getUrl(),
                monitor.getTimeoutMs(),
                Instant.now()
        );
    }
}