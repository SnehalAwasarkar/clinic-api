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
        if (request.dateOfBirth() == null || request.gender() == null) {
            throw new IllegalArgumentException("dateOfBirth and gender are required");
        }
        Patient patient = new Patient(request.firstName(), request.lastName(), request.email(), request.phone(),
                request.dateOfBirth(), request.gender(), request.address());
        return patientRepository.save(patient);
    }

    public Patient getById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found: " + id));
    }

    public List<Patient> listAll(String q) {
        if (q == null || q.isBlank()) {
            return patientRepository.findAll();
        }
        return patientRepository.search(q);
    }

    public Patient update(Long id, PatientRequest request) {
        Patient patient = getById(id);
        patient.setFirstName(request.firstName());
        patient.setLastName(request.lastName());
        patient.setEmail(request.email());
        patient.setPhone(request.phone());

        if (request.dateOfBirth() == null && request.gender() == null && request.address() == null) {
            throw new IllegalArgumentException("At least one of dateOfBirth, gender, or address must be provided");
        }

        if (request.dateOfBirth() != null) {
            patient.setDateOfBirth(request.dateOfBirth());
        }
        if (request.gender() != null) {
            patient.setGender(request.gender());
        }
        if (request.address() != null) {
            patient.setAddress(request.address());
        }

        return patientRepository.save(patient);
    }

    public void delete(Long id) {
        Patient patient = getById(id);
        patientRepository.delete(patient);
    }
}
