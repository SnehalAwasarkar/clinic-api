package com.example.clinic.exception;

public class DoctorLicenseNumberConflictException extends RuntimeException {

    public DoctorLicenseNumberConflictException(String message) {
        super(message);
    }
}
