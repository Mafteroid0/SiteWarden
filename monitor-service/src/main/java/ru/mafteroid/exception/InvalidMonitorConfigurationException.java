package ru.mafteroid.exception;

public class InvalidMonitorConfigurationException extends RuntimeException {
    public InvalidMonitorConfigurationException(String message){
        super("Invalid monitor configuration: " + message);
    }
    public InvalidMonitorConfigurationException(String message, Throwable cause) {
        super("Invalid monitor configuration: " + message, cause);
    }
}
