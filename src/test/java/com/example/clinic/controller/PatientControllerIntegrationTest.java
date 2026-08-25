package com.example.clinic.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.clinic.dto.PatientRequest;
import com.example.clinic.entity.Gender;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
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
class PatientControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createGetListUpdateAndDeletePatient() throws Exception {
        PatientRequest createRequest = new PatientRequest("Ada", "Lovelace", "ada@example.com", "1234567890",
                LocalDate.of(1990, 1, 1), Gender.FEMALE, "123 Main St");

        String createResponse = mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("Ada"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.dateOfBirth").value("1990-01-01"))
                .andExpect(jsonPath("$.gender").value("FEMALE"))
                .andExpect(jsonPath("$.address").value("123 Main St"))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(get("/api/patients/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ada@example.com"))
                .andExpect(jsonPath("$.dateOfBirth").value("1990-01-01"))
                .andExpect(jsonPath("$.gender").value("FEMALE"))
                .andExpect(jsonPath("$.address").value("123 Main St"));

        mockMvc.perform(get("/api/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        PatientRequest updateRequest = new PatientRequest("Ada", "Byron", "ada@example.com", "1234567890",
                LocalDate.of(1990, 1, 1), Gender.FEMALE, "123 Main St");
        mockMvc.perform(put("/api/patients/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName").value("Byron"));

        mockMvc.perform(delete("/api/patients/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/patients/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void createWithInvalidEmailReturnsBadRequest() throws Exception {
        PatientRequest invalidRequest = new PatientRequest("Ada", "Lovelace", "not-an-email", "1234567890",
                LocalDate.of(1990, 1, 1), Gender.FEMALE, "123 Main St");

        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createWithFutureDateOfBirthReturnsBadRequest() throws Exception {
        PatientRequest invalidRequest = new PatientRequest("Ada", "Lovelace", "ada2@example.com", "1234567890",
                LocalDate.now().plusDays(1), Gender.FEMALE, "123 Main St");

        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createWithInvalidGenderReturnsBadRequest() throws Exception {
        String invalidJson = "{\"firstName\":\"Ada\",\"lastName\":\"Lovelace\",\"email\":\"ada3@example.com\","
                + "\"phone\":\"1234567890\",\"dateOfBirth\":\"1990-01-01\",\"gender\":\"UNKNOWN\",\"address\":\"123 Main St\"}";

        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createWithMissingDateOfBirthReturnsBadRequest() throws Exception {
        PatientRequest invalidRequest = new PatientRequest("Ada", "Lovelace", "ada4@example.com", "1234567890",
                null, Gender.FEMALE, "123 Main St");

        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createWithMissingGenderReturnsBadRequest() throws Exception {
        PatientRequest invalidRequest = new PatientRequest("Ada", "Lovelace", "ada5@example.com", "1234567890",
                LocalDate.of(1990, 1, 1), null, "123 Main St");

        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateWithOnlyOneDemographicFieldLeavesOthersUnchanged() throws Exception {
        PatientRequest createRequest = new PatientRequest("Grace", "Hopper", "grace2@example.com", "1234567890",
                LocalDate.of(1985, 5, 5), Gender.FEMALE, "456 Elm St");

        String createResponse = mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(createResponse).get("id").asLong();

        PatientRequest partialUpdate = new PatientRequest("Grace", "Hopper", "grace2@example.com", "1234567890",
                null, null, "789 Oak Ave");

        mockMvc.perform(put("/api/patients/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(partialUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.address").value("789 Oak Ave"))
                .andExpect(jsonPath("$.dateOfBirth").value("1985-05-05"))
                .andExpect(jsonPath("$.gender").value("FEMALE"));
    }

    @Test
    void updateWithNoDemographicFieldsReturnsBadRequest() throws Exception {
        PatientRequest createRequest = new PatientRequest("Alan", "Turing", "alan@example.com", "1234567890",
                LocalDate.of(1912, 6, 23), Gender.MALE, "1 Computing Way");

        String createResponse = mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(createResponse).get("id").asLong();

        PatientRequest emptyUpdate = new PatientRequest("Alan", "Turing", "alan@example.com", "1234567890",
                null, null, null);

        mockMvc.perform(put("/api/patients/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyUpdate)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchByFirstNameSubstringIsCaseInsensitive() throws Exception {
        PatientRequest createRequest = new PatientRequest("Katherine", "Johnson", "katherine@example.com",
                "5551110000", LocalDate.of(1960, 2, 2), Gender.FEMALE, "1 NASA Way");
        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/patients").param("q", "kathe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].firstName").value("Katherine"));
    }

    @Test
    void searchByLastNameSubstringMatches() throws Exception {
        PatientRequest createRequest = new PatientRequest("Dorothy", "Vaughan", "dorothy@example.com",
                "5552220000", LocalDate.of(1955, 3, 3), Gender.FEMALE, "2 NASA Way");
        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/patients").param("q", "vaugh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].lastName").value("Vaughan"));
    }

    @Test
    void searchByCombinedFullNameMatches() throws Exception {
        PatientRequest createRequest = new PatientRequest("Mary", "Jackson", "mary@example.com",
                "5553330000", LocalDate.of(1950, 4, 4), Gender.FEMALE, "3 NASA Way");
        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/patients").param("q", "mary jackson"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].email").value("mary@example.com"));
    }

    @Test
    void searchByEmailSubstringMatches() throws Exception {
        PatientRequest createRequest = new PatientRequest("Nichelle", "Nichols", "nichelle@example.com",
                "5554440000", LocalDate.of(1965, 5, 5), Gender.FEMALE, "4 NASA Way");
        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/patients").param("q", "nichelle@"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].lastName").value("Nichols"));
    }

    @Test
    void searchByPhoneSubstringMatches() throws Exception {
        PatientRequest createRequest = new PatientRequest("Sally", "Ride", "sally@example.com",
                "5559998888", LocalDate.of(1970, 6, 6), Gender.FEMALE, "5 NASA Way");
        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/patients").param("q", "9998"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].phone").value("5559998888"));
    }

    @Test
    void searchWithNoMatchesReturnsEmptyArray() throws Exception {
        PatientRequest createRequest = new PatientRequest("Ellen", "Ochoa", "ellen@example.com",
                "5556660000", LocalDate.of(1975, 7, 7), Gender.FEMALE, "6 NASA Way");
        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/patients").param("q", "zzz-no-such-match-zzz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void searchWithNoQueryParamReturnsUnchangedFullList() throws Exception {
        PatientRequest first = new PatientRequest("Peggy", "Whitson", "peggy@example.com",
                "5557770000", LocalDate.of(1980, 8, 8), Gender.FEMALE, "7 NASA Way");
        PatientRequest second = new PatientRequest("Chris", "Hadfield", "chris@example.com",
                "5558880000", LocalDate.of(1980, 9, 9), Gender.MALE, "8 NASA Way");

        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(first)))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(second)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/patients").param("q", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void searchWithSpecialCharactersIsTreatedLiterallyAndDoesNotError() throws Exception {
        PatientRequest createRequest = new PatientRequest("Valentina", "Tereshkova", "valentina@example.com",
                "5551234000", LocalDate.of(1937, 3, 6), Gender.FEMALE, "9 NASA Way");
        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/patients").param("q", "100%"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        mockMvc.perform(get("/api/patients").param("q", "val_ntina"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        mockMvc.perform(get("/api/patients").param("q", "O'Brien"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
