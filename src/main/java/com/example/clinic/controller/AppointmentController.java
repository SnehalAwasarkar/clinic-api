package com.example.clinic.controller;

import com.example.clinic.dto.AppointmentRequest;
import com.example.clinic.dto.AppointmentResponse;
import com.example.clinic.service.AppointmentService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    public ResponseEntity<AppointmentResponse> create(@Valid @RequestBody AppointmentRequest request) {
        AppointmentResponse response = AppointmentResponse.from(appointmentService.create(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public AppointmentResponse getById(@PathVariable Long id) {
        return AppointmentResponse.from(appointmentService.getById(id));
    }

    @GetMapping
    public List<AppointmentResponse> listAll() {
        return appointmentService.listAll().stream().map(AppointmentResponse::from).toList();
    }

    @PutMapping("/{id}")
    public AppointmentResponse update(@PathVariable Long id, @Valid @RequestBody AppointmentRequest request) {
        return AppointmentResponse.from(appointmentService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        appointmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
