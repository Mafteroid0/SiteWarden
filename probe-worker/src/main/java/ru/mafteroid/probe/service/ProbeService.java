package ru.mafteroid.probe.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.mafteroid.events.CheckCompletedEvent;
import ru.mafteroid.events.CheckType;
import ru.mafteroid.probe.client.ProbeClient;
import ru.mafteroid.events.CheckRequestedEvent;
import ru.mafteroid.probe.messaging.CheckCompletedEventFactory;
import ru.mafteroid.probe.messaging.CheckCompletedPublisher;
import ru.mafteroid.probe.model.ProbeResult;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class ProbeService {

    private final List<ProbeClient> clients;
    private final CheckCompletedPublisher checkCompletedPublisher;

    public ProbeService(List<ProbeClient> clients, CheckCompletedPublisher checkCompletedPublisher) {
        this.clients = clients;
        this.checkCompletedPublisher = checkCompletedPublisher;
    }

    private ProbeClient resolveClient(CheckType checkType) {
        List<ProbeClient> matchingClients = clients.stream()
                .filter(client -> client.supports(checkType))
                .toList();

        if (matchingClients.size() != 1) {
            throw new IllegalStateException(
                    "Expected exactly one client for " + checkType
                            + ", found: " + matchingClients.size()
            );
        }

        return matchingClients.getFirst();
    }

    private static final Logger log =
            LoggerFactory.getLogger(ProbeService.class);

    public void execute(CheckRequestedEvent event) {
        ProbeClient client = resolveClient(event.checkType());

        URI target = URI.create(event.url());
        Duration timeout = Duration.ofMillis(event.timeoutMs());

        try {

            Instant startedAt = Instant.now();
            ProbeResult result = client.probe(target, timeout);
            Instant completedAt = Instant.now();

            CheckCompletedEvent checkCompletedEvent = CheckCompletedEventFactory.from(
                    event,
                    result,
                    startedAt,
                    completedAt
            );

            log.info(
                    "Check completed: checkId={}, result={}",
                    event.checkId(),
                    result
            );
            checkCompletedPublisher.publish(checkCompletedEvent);


        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "Check interrupted: " + event.checkId(),
                    exception
            );
        }
    }
}
