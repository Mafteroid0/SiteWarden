package ru.mafteroid.web.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateMonitorStateRequest(
        @NotNull
        Boolean enabled
) {
}
