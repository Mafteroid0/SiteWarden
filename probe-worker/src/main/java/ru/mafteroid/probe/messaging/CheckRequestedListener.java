package ru.mafteroid.probe.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.mafteroid.events.CheckRequestedEvent;
import ru.mafteroid.probe.service.ProbeService;

@Component
public class CheckRequestedListener {

    private final ProbeService probeService;

    public CheckRequestedListener(ProbeService probeService) {
        this.probeService = probeService;
    }

    private static final Logger log =
            LoggerFactory.getLogger(CheckRequestedListener.class);

    @KafkaListener(
            topics = "check.requested",
            groupId = "probe-workers"
    )
    public void listen(CheckRequestedEvent event) {
        probeService.execute(event);

        log.info(
                "Received checkId={}, monitorId={}, url={}, timeoutMs={}",
                event.checkId(),
                event.monitorId(),
                event.url(),
                event.timeoutMs()
        );
    }
}