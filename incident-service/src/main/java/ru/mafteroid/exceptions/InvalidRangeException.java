package ru.mafteroid.exceptions;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Wrong range")
public class InvalidRangeException extends RuntimeException {
    public InvalidRangeException(String message) {
        super("Wrong Range: " + message);
    }
}
