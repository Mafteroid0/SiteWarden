package ru.mafteroid.probe.messaging;

import ru.mafteroid.events.CheckCompletedEvent;
import ru.mafteroid.events.CheckRequestedEvent;
import ru.mafteroid.probe.model.ProbeResult;

import java.time.Instant;

public class CheckCompletedEventFactory {
    public static CheckCompletedEvent from(CheckRequestedEvent event,
                                           ProbeResult result,
                                           Instant startedAt,
                                           Instant completedAt){
        return new CheckCompletedEvent(
                event.checkId(),
                event.monitorId(),
                event.checkType(),
                startedAt,
                completedAt,
                result.successful(),
                result.httpStatus(),
                result.durationMs(),
                result.errorCode()
        );
    }
}
