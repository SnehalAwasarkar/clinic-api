package com.example.clinic.service;

import com.example.clinic.dto.AppointmentRequest;
import com.example.clinic.entity.Appointment;
import com.example.clinic.entity.Doctor;
import com.example.clinic.entity.Patient;
import com.example.clinic.exception.ResourceNotFoundException;
import com.example.clinic.repository.AppointmentRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientService patientService;
    private final DoctorService doctorService;

    public AppointmentService(AppointmentRepository appointmentRepository, PatientService patientService,
                               DoctorService doctorService) {
        this.appointmentRepository = appointmentRepository;
        this.patientService = patientService;
        this.doctorService = doctorService;
    }

    public Appointment create(AppointmentRequest request) {
        Patient patient = patientService.getById(request.patientId());
        Doctor doctor = doctorService.getById(request.doctorId());
        Appointment appointment = new Appointment(patient, doctor, request.appointmentDate(), request.reason());
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
        Doctor doctor = doctorService.getById(request.doctorId());
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDate(request.appointmentDate());
        appointment.setReason(request.reason());
        return appointmentRepository.save(appointment);
    }

    public void delete(Long id) {
        Appointment appointment = getById(id);
        appointmentRepository.delete(appointment);
    }
}
