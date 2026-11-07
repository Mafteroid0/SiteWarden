package ru.mafteroid;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Limit;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.mafteroid.events.CheckCompletedEvent;
import ru.mafteroid.events.CheckType;
import ru.mafteroid.repository.CheckResultRepository;
import ru.mafteroid.repository.IncidentRepository;
import ru.mafteroid.repository.MonitorStatusRepository;
import ru.mafteroid.service.IncidentService;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.kafka.listener.auto-startup=false")
@AutoConfigureMockMvc
@Testcontainers
class IncidentServiceIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired IncidentService service;
    @Autowired CheckResultRepository checks;
    @Autowired MonitorStatusRepository statuses;
    @Autowired IncidentRepository incidents;
    @Autowired MockMvc mvc;

    private final UUID monitorId = UUID.randomUUID();
    private final Instant start = Instant.parse("2026-11-07T12:00:00Z");

    @Test
    void opensOneIncidentAndClosesItOnRecovery() {
        var failure = eventAt(1, false);
        service.process(eventAt(0, true));
        service.process(failure);
        service.process(eventAt(2, false));

        var open = incidents.findByMonitorIdAndResolvedAtIsNull(monitorId).orElseThrow();
        assertEquals(failure.completedAt(), open.getOpenedAt().toInstant());
        assertEquals(start.plusSeconds(2), lastCheckedAt());

        var recovery = eventAt(3, true);
        service.process(recovery);
        service.process(failure);

        var history = incidents.findByMonitorIdOrderByOpenedAtDesc(monitorId, Limit.of(10));
        assertEquals(1, history.size());
        assertEquals(recovery.completedAt(), history.getFirst().getResolvedAt().toInstant());
        assertEquals("UP", statuses.findById(monitorId).orElseThrow().getStatus());
        assertEquals(recovery.completedAt(), lastCheckedAt());
        assertEquals(4, checks.findByMonitorIdOrderByCompletedAtDesc(monitorId, Limit.of(10)).size());
    }

    @Test
    void keepsLateSuccessInHistoryWithoutClosingCurrentIncident() {
        var failure = eventAt(5, false);
        var olderSuccess = eventAt(1, true);
        service.process(failure);
        service.process(olderSuccess);

        assertTrue(checks.existsById(olderSuccess.checkId()));
        assertEquals("DOWN", statuses.findById(monitorId).orElseThrow().getStatus());
        assertEquals(failure.completedAt(), lastCheckedAt());
        assertTrue(incidents.findByMonitorIdAndResolvedAtIsNull(monitorId).isPresent());
    }

    @Test
    void returnsUnknownBeforeFirstCheck() throws Exception {
        mvc.perform(get("/{id}/status", monitorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UNKNOWN"));
    }

    @Test
    void returnsNewestCheckAndRejectsInvalidLimit() throws Exception {
        service.process(eventAt(0, true));
        var newest = eventAt(1, false);
        service.process(newest);

        mvc.perform(get("/{id}/checks", monitorId).param("limit", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(newest.checkId().toString()));

        mvc.perform(get("/{id}/checks", monitorId).param("limit", "0"))
                .andExpect(status().isBadRequest());
    }

    private Instant lastCheckedAt() {
        return statuses.findById(monitorId).orElseThrow().getLastCheckedAt().toInstant();
    }

    private CheckCompletedEvent eventAt(int second, boolean successful) {
        Instant end = start.plusSeconds(second);
        return new CheckCompletedEvent(UUID.randomUUID(), monitorId, CheckType.HTTP,
                end.minusMillis(50), end, successful, successful ? 200 : 500,
                50, successful ? null : "HTTP_STATUS");
    }
}
