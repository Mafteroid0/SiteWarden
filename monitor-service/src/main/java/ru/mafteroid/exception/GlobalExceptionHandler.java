package ru.mafteroid.exception;


import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidMonitorConfigurationException.class)
    public ProblemDetail handleInvalidConfiguration(
            InvalidMonitorConfigurationException ex
    ) {
        ProblemDetail problem = ProblemDetail.forStatus(
                HttpStatus.BAD_REQUEST
        );

        problem.setTitle("Invalid monitor configuration");
        problem.setDetail(ex.getMessage());

        return problem;
    }

    @ExceptionHandler(MonitorNotFoundException.class)
    public ProblemDetail handleMonitorNotFound(
            MonitorNotFoundException ex
    ) {
        ProblemDetail problem = ProblemDetail.forStatus(
                HttpStatus.NOT_FOUND
        );

        problem.setTitle("Monitor not found");
        problem.setDetail(ex.getMessage());

        return problem;
    }
}