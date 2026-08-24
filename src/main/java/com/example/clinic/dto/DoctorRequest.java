package com.example.clinic.dto;

import com.example.clinic.entity.DoctorStatus;
import jakarta.validation.constraints.NotBlank;

public record DoctorRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        String specialization,
        @NotBlank String licenseNumber,
        DoctorStatus status
) {
}
