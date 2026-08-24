package com.example.clinic.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.clinic.dto.AppointmentRequest;
import com.example.clinic.entity.Doctor;
import com.example.clinic.entity.DoctorStatus;
import com.example.clinic.entity.Gender;
import com.example.clinic.entity.Patient;
import com.example.clinic.repository.DoctorRepository;
import com.example.clinic.repository.PatientRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AppointmentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    private Long patientId;
    private Long doctorId;

    @BeforeEach
    void createPatientAndDoctor() {
        Patient patient = patientRepository.save(
                new Patient("Grace", "Hopper", "grace@example.com", "5551234567",
                        LocalDate.of(1980, 12, 9), Gender.FEMALE, "123 Main St"));
        patientId = patient.getId();

        Doctor doctor = doctorRepository.save(
                new Doctor("John", "Watson", "General Medicine", "LIC-1000", DoctorStatus.ACTIVE));
        doctorId = doctor.getId();
    }

    @Test
    void createGetListUpdateAndDeleteAppointment() throws Exception {
        LocalDateTime when = LocalDateTime.of(2026, 9, 1, 10, 30);
        AppointmentRequest createRequest = new AppointmentRequest(patientId, doctorId, when, "Annual checkup");

        String createResponse = mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reason").value("Annual checkup"))
                .andExpect(jsonPath("$.status").value("SCHEDULED"))
                .andExpect(jsonPath("$.patientId").value(patientId))
                .andExpect(jsonPath("$.doctorId").value(doctorId))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(get("/api/appointments/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reason").value("Annual checkup"))
                .andExpect(jsonPath("$.doctorId").value(doctorId));

        mockMvc.perform(get("/api/appointments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        AppointmentRequest updateRequest = new AppointmentRequest(patientId, doctorId, when.plusDays(1), "Follow-up");
        mockMvc.perform(put("/api/appointments/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reason").value("Follow-up"))
                .andExpect(jsonPath("$.doctorId").value(doctorId));

        mockMvc.perform(delete("/api/appointments/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/appointments/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void createWithUnknownPatientReturnsNotFound() throws Exception {
        AppointmentRequest request = new AppointmentRequest(999999L, doctorId, LocalDateTime.now().plusDays(1), "Checkup");

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createWithUnknownDoctorReturnsNotFound() throws Exception {
        AppointmentRequest request = new AppointmentRequest(patientId, 999999L, LocalDateTime.now().plusDays(1), "Checkup");

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createWithMissingDoctorIdReturnsBadRequest() throws Exception {
        AppointmentRequest request = new AppointmentRequest(patientId, null, LocalDateTime.now().plusDays(1), "Checkup");

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
