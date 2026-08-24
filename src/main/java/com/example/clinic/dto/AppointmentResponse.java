package com.example.clinic.dto;

import com.example.clinic.entity.Appointment;
import java.time.LocalDateTime;

public record AppointmentResponse(
        Long id,
        Long patientId,
        LocalDateTime appointmentDate,
        String reason,
        String status
) {
    public static AppointmentResponse from(Appointment appointment) {
        return new AppointmentResponse(
                appointment.getId(),
                appointment.getPatient().getId(),
                appointment.getAppointmentDate(),
                appointment.getReason(),
                appointment.getStatus().name()
        );
    }
}
