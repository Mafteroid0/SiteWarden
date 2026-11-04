package ru.mafteroid.probe.client;

import ru.mafteroid.events.CheckType;
import ru.mafteroid.probe.model.ProbeResult;

import java.net.URI;
import java.time.Duration;

public interface ProbeClient {

    boolean supports(CheckType checkType);

    ProbeResult probe(URI target, Duration timeout)
            throws InterruptedException;
}