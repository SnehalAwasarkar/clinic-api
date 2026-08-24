package com.example.clinic.service;

import com.example.clinic.dto.DoctorRequest;
import com.example.clinic.entity.Doctor;
import com.example.clinic.entity.DoctorStatus;
import com.example.clinic.exception.DoctorHasAppointmentsException;
import com.example.clinic.exception.DoctorLicenseNumberConflictException;
import com.example.clinic.exception.ResourceNotFoundException;
import com.example.clinic.repository.AppointmentRepository;
import com.example.clinic.repository.DoctorRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;

    public DoctorService(DoctorRepository doctorRepository, AppointmentRepository appointmentRepository) {
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
    }

    public Doctor create(DoctorRequest request) {
        if (doctorRepository.existsByLicenseNumber(request.licenseNumber())) {
            throw new DoctorLicenseNumberConflictException(
                    "Doctor with licenseNumber " + request.licenseNumber() + " already exists");
        }
        DoctorStatus status = request.status() != null ? request.status() : DoctorStatus.ACTIVE;
        Doctor doctor = new Doctor(request.firstName(), request.lastName(), request.specialization(),
                request.licenseNumber(), status);
        return doctorRepository.save(doctor);
    }

    public Doctor getById(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found: " + id));
    }

    public List<Doctor> listAll() {
        return doctorRepository.findAll();
    }

    public Doctor update(Long id, DoctorRequest request) {
        Doctor doctor = getById(id);

        if (doctorRepository.existsByLicenseNumberAndIdNot(request.licenseNumber(), id)) {
            throw new DoctorLicenseNumberConflictException(
                    "Doctor with licenseNumber " + request.licenseNumber() + " already exists");
        }

        doctor.setFirstName(request.firstName());
        doctor.setLastName(request.lastName());
        doctor.setSpecialization(request.specialization());
        doctor.setLicenseNumber(request.licenseNumber());
        doctor.setStatus(request.status() != null ? request.status() : DoctorStatus.ACTIVE);

        return doctorRepository.save(doctor);
    }

    public void delete(Long id) {
        Doctor doctor = getById(id);
        if (appointmentRepository.existsByDoctorId(id)) {
            throw new DoctorHasAppointmentsException(
                    "Cannot delete doctor " + id + ": doctor has existing appointments");
        }
        doctorRepository.delete(doctor);
    }
}
