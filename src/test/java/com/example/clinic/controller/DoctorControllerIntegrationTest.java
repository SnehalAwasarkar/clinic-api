package com.example.clinic.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.clinic.dto.AppointmentRequest;
import com.example.clinic.dto.DoctorRequest;
import com.example.clinic.entity.DoctorStatus;
import com.example.clinic.entity.Gender;
import com.example.clinic.entity.Patient;
import com.example.clinic.repository.PatientRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
class DoctorControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PatientRepository patientRepository;

    @Test
    void createGetListUpdateAndDeleteDoctor() throws Exception {
        DoctorRequest createRequest = new DoctorRequest("Meredith", "Grey", "Surgery", "LIC-2000", null);

        String createResponse = mockMvc.perform(post("/api/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("Meredith"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.licenseNumber").value("LIC-2000"))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(get("/api/doctors/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName").value("Grey"));

        mockMvc.perform(get("/api/doctors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        DoctorRequest updateRequest = new DoctorRequest("Meredith", "Shepherd", "Neurosurgery", "LIC-2000", DoctorStatus.ACTIVE);
        mockMvc.perform(put("/api/doctors/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName").value("Shepherd"))
                .andExpect(jsonPath("$.specialization").value("Neurosurgery"));

        mockMvc.perform(delete("/api/doctors/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/doctors/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void createWithDuplicateLicenseNumberReturnsConflict() throws Exception {
        DoctorRequest first = new DoctorRequest("Derek", "Shepherd", "Neurosurgery", "LIC-3000", null);
        mockMvc.perform(post("/api/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(first)))
                .andExpect(status().isCreated());

        DoctorRequest duplicate = new DoctorRequest("Other", "Doctor", "Cardiology", "LIC-3000", null);
        mockMvc.perform(post("/api/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isConflict());
    }

    @Test
    void createWithInvalidStatusReturnsBadRequest() throws Exception {
        String invalidJson = "{\"firstName\":\"Cristina\",\"lastName\":\"Yang\",\"specialization\":\"Cardio\","
                + "\"licenseNumber\":\"LIC-4000\",\"status\":\"UNKNOWN\"}";

        mockMvc.perform(post("/api/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteDoctorWithExistingAppointmentReturnsConflict() throws Exception {
        DoctorRequest doctorRequest = new DoctorRequest("Miranda", "Bailey", "General Surgery", "LIC-5000", null);
        String createResponse = mockMvc.perform(post("/api/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(doctorRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long doctorId = objectMapper.readTree(createResponse).get("id").asLong();

        Patient patient = patientRepository.save(
                new Patient("Izzie", "Stevens", "izzie@example.com", "1112223333",
                        LocalDate.of(1982, 3, 3), Gender.FEMALE, "1 Grey Sloan Way"));

        AppointmentRequest appointmentRequest = new AppointmentRequest(patient.getId(), doctorId,
                LocalDateTime.now().plusDays(1), "Consultation");
        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(appointmentRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/doctors/{id}", doctorId))
                .andExpect(status().isConflict());
    }

    @Test
    void deleteDoctorWithoutAppointmentsSucceeds() throws Exception {
        DoctorRequest doctorRequest = new DoctorRequest("George", "O'Malley", "Trauma", "LIC-6000", null);
        String createResponse = mockMvc.perform(post("/api/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(doctorRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long doctorId = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(delete("/api/doctors/{id}", doctorId))
                .andExpect(status().isNoContent());
    }
}
