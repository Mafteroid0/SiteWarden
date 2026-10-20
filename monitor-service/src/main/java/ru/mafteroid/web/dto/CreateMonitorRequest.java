package ru.mafteroid.web.dto;

import jakarta.validation.constraints.*;

public record CreateMonitorRequest(
        @NotBlank
        @Size(max = 100)
        String name,

        @NotBlank
        @Size(max = 2048)
        String url,

        @NotNull
        @Min(30)
        Integer intervalSeconds,

        @NotNull
        @Min(1000)
        @Max(10000)
        Integer timeoutMs
) {}