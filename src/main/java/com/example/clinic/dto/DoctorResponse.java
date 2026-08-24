package com.example.clinic.dto;

import com.example.clinic.entity.Doctor;

public record DoctorResponse(
        Long id,
        String firstName,
        String lastName,
        String specialization,
        String licenseNumber,
        String status
) {
    public static DoctorResponse from(Doctor doctor) {
        return new DoctorResponse(
                doctor.getId(),
                doctor.getFirstName(),
                doctor.getLastName(),
                doctor.getSpecialization(),
                doctor.getLicenseNumber(),
                doctor.getStatus().name()
        );
    }
}
