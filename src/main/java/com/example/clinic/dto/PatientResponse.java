package com.example.clinic.dto;

import com.example.clinic.entity.Patient;
import java.time.LocalDate;

public record PatientResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phone,
        LocalDate dateOfBirth,
        String gender,
        String address,
        String status
) {
    public static PatientResponse from(Patient patient) {
        return new PatientResponse(
                patient.getId(),
                patient.getFirstName(),
                patient.getLastName(),
                patient.getEmail(),
                patient.getPhone(),
                patient.getDateOfBirth(),
                patient.getGender() != null ? patient.getGender().name() : null,
                patient.getAddress(),
                patient.getStatus().name()
        );
    }
}
