package com.example.clinic.service;

import com.example.clinic.dto.PatientRequest;
import com.example.clinic.entity.Patient;
import com.example.clinic.exception.ResourceNotFoundException;
import com.example.clinic.repository.PatientRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public Patient create(PatientRequest request) {
        Patient patient = new Patient(request.firstName(), request.lastName(), request.email(), request.phone());
        return patientRepository.save(patient);
    }

    public Patient getById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found: " + id));
    }

    public List<Patient> listAll() {
        return patientRepository.findAll();
    }

    public Patient update(Long id, PatientRequest request) {
        Patient patient = getById(id);
        patient.setFirstName(request.firstName());
        patient.setLastName(request.lastName());
        patient.setEmail(request.email());
        patient.setPhone(request.phone());
        return patientRepository.save(patient);
    }

    public void delete(Long id) {
        Patient patient = getById(id);
        patientRepository.delete(patient);
    }
}
