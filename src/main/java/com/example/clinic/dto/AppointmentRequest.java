package com.example.clinic.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record AppointmentRequest(
        @NotNull Long patientId,
        @NotNull LocalDateTime appointmentDate,
        String reason
) {
}
