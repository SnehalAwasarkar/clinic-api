package com.example.clinic.dto;

import com.example.clinic.entity.Patient;

public record PatientResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phone,
        String status
) {
    public static PatientResponse from(Patient patient) {
        return new PatientResponse(
                patient.getId(),
                patient.getFirstName(),
                patient.getLastName(),
                patient.getEmail(),
                patient.getPhone(),
                patient.getStatus().name()
        );
    }
}
