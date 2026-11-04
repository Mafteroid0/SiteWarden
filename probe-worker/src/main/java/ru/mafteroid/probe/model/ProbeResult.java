package ru.mafteroid.probe.model;

public record ProbeResult(
        boolean successful,
        Integer httpStatus,
        long durationMs,
        String errorCode
) {}