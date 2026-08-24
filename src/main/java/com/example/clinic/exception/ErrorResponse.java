package com.example.clinic.exception;

public record ErrorResponse(
        int status,
        String message
) {
}
