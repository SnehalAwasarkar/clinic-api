package com.example.clinic.service;

import com.example.clinic.dto.AppointmentRequest;
import com.example.clinic.entity.Appointment;
import com.example.clinic.entity.Patient;
import com.example.clinic.exception.ResourceNotFoundException;
import com.example.clinic.repository.AppointmentRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientService patientService;

    public AppointmentService(AppointmentRepository appointmentRepository, PatientService patientService) {
        this.appointmentRepository = appointmentRepository;
        this.patientService = patientService;
    }

    public Appointment create(AppointmentRequest request) {
        Patient patient = patientService.getById(request.patientId());
        Appointment appointment = new Appointment(patient, request.appointmentDate(), request.reason());
        return appointmentRepository.save(appointment);
    }

    public Appointment getById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found: " + id));
    }

    public List<Appointment> listAll() {
        return appointmentRepository.findAll();
    }

    public Appointment update(Long id, AppointmentRequest request) {
        Appointment appointment = getById(id);
        Patient patient = patientService.getById(request.patientId());
        appointment.setPatient(patient);
        appointment.setAppointmentDate(request.appointmentDate());
        appointment.setReason(request.reason());
        return appointmentRepository.save(appointment);
    }

    public void delete(Long id) {
        Appointment appointment = getById(id);
        appointmentRepository.delete(appointment);
    }
}
