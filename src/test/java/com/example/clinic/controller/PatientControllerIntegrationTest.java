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
    void searchByFirstNameIsCaseInsensitiveAndPartial() throws Exception {
        PatientRequest createRequest = new PatientRequest("Rosalind", "Franklin", "rosalind@example.com",
                "5551112222", LocalDate.of(1970, 2, 2), Gender.FEMALE, "1 DNA Way");
        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/patients").param("q", "rosa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].firstName").value("Rosalind"));
    }

    @Test
    void searchByLastNameIsCaseInsensitiveAndPartial() throws Exception {
        PatientRequest createRequest = new PatientRequest("Katherine", "Johnson", "katherine@example.com",
                "5553334444", LocalDate.of(1975, 3, 3), Gender.FEMALE, "1 NASA Way");
        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/patients").param("q", "JOHN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].lastName").value("Johnson"));
    }

    @Test
    void searchByCombinedFullNameMatches() throws Exception {
        PatientRequest createRequest = new PatientRequest("Marie", "Curie", "marie@example.com",
                "5555556666", LocalDate.of(1965, 4, 4), Gender.FEMALE, "1 Radium Way");
        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/patients").param("q", "marie curie"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].firstName").value("Marie"));
    }

    @Test
    void searchByEmailIsCaseInsensitiveAndPartial() throws Exception {
        PatientRequest createRequest = new PatientRequest("Barbara", "McClintock", "barbara.mcclintock@example.com",
                "5557778888", LocalDate.of(1960, 5, 5), Gender.FEMALE, "1 Genetics Way");
        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/patients").param("q", "MCCLINTOCK@EXAMPLE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].email").value("barbara.mcclintock@example.com"));
    }

    @Test
    void searchByPhoneIsPartial() throws Exception {
        PatientRequest createRequest = new PatientRequest("Chien-Shiung", "Wu", "chien@example.com",
                "5559990000", LocalDate.of(1955, 6, 6), Gender.FEMALE, "1 Physics Way");
        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/patients").param("q", "9990"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].phone").value("5559990000"));
    }

    @Test
    void noQueryParamReturnsUnchangedFullList() throws Exception {
        PatientRequest createRequest = new PatientRequest("Hedy", "Lamarr", "hedy@example.com",
                "5551230000", LocalDate.of(1950, 7, 7), Gender.FEMALE, "1 Invention Way");
        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void searchWithNoMatchesReturnsEmptyList() throws Exception {
        PatientRequest createRequest = new PatientRequest("Emmy", "Noether", "emmy@example.com",
                "5554440000", LocalDate.of(1945, 8, 8), Gender.FEMALE, "1 Algebra Way");
        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/patients").param("q", "zzz-nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void searchWithSpecialCharactersIsTreatedLiterallyAndDoesNotError() throws Exception {
        PatientRequest createRequest = new PatientRequest("O'Brien", "Smith", "obrien@example.com",
                "5552220000", LocalDate.of(1940, 9, 9), Gender.MALE, "1 Special Way");
        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/patients").param("q", "O'Brien"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].firstName").value("O'Brien"));

        mockMvc.perform(get("/api/patients").param("q", "%_'"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
