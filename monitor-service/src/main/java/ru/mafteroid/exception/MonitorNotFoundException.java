package ru.mafteroid.exception;

import java.util.UUID;

public class MonitorNotFoundException extends RuntimeException {
    public MonitorNotFoundException(UUID uuid){
        super("Monitor with id " + uuid + " not found");

    }
}
