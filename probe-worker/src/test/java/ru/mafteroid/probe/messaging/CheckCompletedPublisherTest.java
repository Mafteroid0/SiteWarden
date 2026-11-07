package ru.mafteroid.probe.messaging;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import ru.mafteroid.events.CheckCompletedEvent;
import ru.mafteroid.events.CheckType;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckCompletedPublisherTest {

    @Mock KafkaTemplate<String, CheckCompletedEvent> kafka;

    @Test
    void propagatesFailedPublication() {
        var now = Instant.now();
        var event = new CheckCompletedEvent(UUID.randomUUID(), UUID.randomUUID(),
                CheckType.HTTP, now, now, true, 200, 0, null);
        when(kafka.send("check.completed", event.monitorId().toString(), event))
                .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("Broker unavailable")));

        var publisher = new CheckCompletedPublisher(kafka);
        assertThrows(IllegalStateException.class, () -> publisher.publish(event));
    }
}
