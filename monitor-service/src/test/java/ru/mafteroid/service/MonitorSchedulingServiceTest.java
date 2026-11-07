package ru.mafteroid.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ru.mafteroid.entity.Monitor;
import ru.mafteroid.events.CheckRequestedEvent;
import ru.mafteroid.events.CheckType;
import ru.mafteroid.messaging.CheckRequestPublisher;
import ru.mafteroid.repository.MonitorRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MonitorSchedulingServiceTest {

    @Test
    void publishesDueMonitorAndAdvancesItsSchedule() {
        var repository = mock(MonitorRepository.class);
        var publisher = mock(CheckRequestPublisher.class);
        var monitor = Monitor.create("Example", "https://example.com", 30, 1000, CheckType.HTTP);
        monitor.setId(UUID.randomUUID());
        monitor.setNextCheckAt(Instant.EPOCH);
        when(repository.findAllByEnabledTrueAndNextCheckAtLessThanEqual(any())).thenReturn(List.of(monitor));

        var before = Instant.now();
        new MonitorSchedulingService(repository, publisher).scheduleDueMonitors();
        var after = Instant.now();

        var sent = ArgumentCaptor.forClass(CheckRequestedEvent.class);
        verify(publisher).publish(sent.capture());
        var event = sent.getValue();
        assertNotNull(event.checkId());
        assertEquals(monitor.getId(), event.monitorId());
        assertEquals(monitor.getUrl(), event.url());
        assertEquals(monitor.getTimeoutMs(), event.timeoutMs());
        assertEquals(CheckType.HTTP, event.checkType());
        assertFalse(monitor.getNextCheckAt().isBefore(before.plusSeconds(30)));
        assertFalse(monitor.getNextCheckAt().isAfter(after.plusSeconds(30)));
    }
}
