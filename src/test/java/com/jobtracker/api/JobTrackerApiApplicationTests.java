package com.jobtracker.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jobtracker.api.dto.JobApplicationRequest;
import com.jobtracker.api.dto.auth.AuthRequest;
import com.jobtracker.api.dto.auth.RegisterRequest;
import com.jobtracker.api.model.ApplicationStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class JobTrackerApiApplicationTests {
    @Autowired
    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Test
    void getApplications_withoutToken_shouldReturn401() throws Exception {
        mockMvc.perform(get("/applications"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registerAndLogin_shouldReturnJwtToken() throws Exception {
        String token = registerAndGetToken(uniqueEmail("user1"));
        assertThat(token).isNotBlank();
    }

    @Test
    void createApplication_withToken_shouldReturn201() throws Exception {
        String token = registerAndGetToken(uniqueEmail("user2"));
        JobApplicationRequest request = new JobApplicationRequest(
                "ING",
                "Java Developer",
                "https://ing.com",
                "Jan de Vries",
                LocalDate.of(2026, 1, 2),
                null,
                ApplicationStatus.APPLIED,
                "Applied via LinkedIn"
        );

        mockMvc.perform(post("/applications")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(header().string("Location", containsString("/applications/")));
    }

    @Test
    void getApplications_withToken_shouldReturnCurrentUsersApplications() throws Exception {
        String token = registerAndGetToken(uniqueEmail("user3"));
        JobApplicationRequest request = new JobApplicationRequest(
                "ING",
                "Java Developer",
                "https://ing.com",
                "Jan de Vries",
                LocalDate.of(2026, 1, 2),
                null,
                ApplicationStatus.APPLIED,
                "Applied via LinkedIn"
        );

        mockMvc.perform(post("/applications")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
        mockMvc.perform(get("/applications")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$[0].companyName").value("ING"))
                .andExpect(jsonPath("$[0].jobTitle").value("Java Developer"))
                .andExpect(jsonPath("$[0].status").value("APPLIED"));
        ;
    }

    @Test
    void getJobApplicationById_withDifferentUserToken_shouldReturn404() throws Exception {
        String token1 = registerAndGetToken(uniqueEmail("user4"));
        String token2 = registerAndGetToken(uniqueEmail("user5"));
        JobApplicationRequest request = new JobApplicationRequest(
                "XYZ",
                "Java Developer",
                "https://ing.com",
                "Jan de Vries",
                LocalDate.of(2026, 1, 2),
                null,
                ApplicationStatus.APPLIED,
                "Applied via LinkedIn"
        );

        MvcResult mvcResult = mockMvc.perform(post("/applications")
                        .header("Authorization", "Bearer " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isCreated()).andReturn();
        String location = mvcResult.getResponse().getHeader("Location");
        assertThat(location).isNotBlank();
        mockMvc.perform(get(location)
                        .header("Authorization", "Bearer " + token2))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateJobApplicationById_withDifferentUserToken_shouldReturn404() throws Exception {
        String token1 = registerAndGetToken(uniqueEmail("user6"));
        String token2 = registerAndGetToken(uniqueEmail("user7"));
        JobApplicationRequest request = new JobApplicationRequest(
                "XYZ",
                "Java Developer",
                "https://ing.com",
                "Jan de Vries",
                LocalDate.of(2026, 1, 2),
                null,
                ApplicationStatus.APPLIED,
                "Applied via LinkedIn"
        );
        JobApplicationRequest updateRequest = new JobApplicationRequest(
                "XYZ Updated",
                "Senior Java Developer",
                "https://ing.com/updated",
                "Jan de Vries",
                LocalDate.of(2026, 1, 2),
                LocalDate.of(2026, 1, 10),
                ApplicationStatus.INTERVIEWING,
                "Updated by another user"
        );

        MvcResult mvcResult = mockMvc.perform(post("/applications")
                        .header("Authorization", "Bearer " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated()).andReturn();
        String location = mvcResult.getResponse().getHeader("Location");
        assertThat(location).isNotBlank();

        mockMvc.perform(MockMvcRequestBuilders.put(location)
                        .header("Authorization", "Bearer " + token2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteJobApplication_withDifferentUserToken_shouldReturn404() throws Exception {
        String token1 = registerAndGetToken(uniqueEmail("user8"));
        String token2 = registerAndGetToken(uniqueEmail("user9"));
        JobApplicationRequest request = new JobApplicationRequest(
                "XYZ",
                "Java Developer",
                "https://ing.com",
                "Jan de Vries",
                LocalDate.of(2026, 1, 2),
                null,
                ApplicationStatus.APPLIED,
                "Applied via LinkedIn"
        );

        MvcResult mvcResult = mockMvc.perform(post("/applications")
                        .header("Authorization", "Bearer " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isCreated()).andReturn();
        String location = mvcResult.getResponse().getHeader("Location");
        assertThat(location).isNotBlank();
        mockMvc.perform(delete(location)
                        .header("Authorization", "Bearer " + token2))
                .andExpect(status().isNotFound());
    }


    private String registerAndGetToken(String email) throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(
                email,
                "password",
                "test",
                "sample"
        );

        AuthRequest authRequest = new AuthRequest(
                email,
                "password"
        );
        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();
        return objectMapper.readTree(mvcResult.getResponse().getContentAsString()).get("token").asText();
    }

    private String uniqueEmail(String prefix) {
        return prefix + "+" + System.nanoTime() + "@test.com";
    }
}
