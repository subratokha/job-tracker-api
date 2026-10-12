package com.jobtracker.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jobtracker.application.dto.JobApplicationRequest;
import com.jobtracker.application.dto.JobApplicationResponse;
import com.jobtracker.application.exception.ResourceNotFoundException;
import com.jobtracker.application.model.ApplicationStatus;
import com.jobtracker.application.service.JobApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
class JobApplicationControllerTest {

    private MockMvc mockMvc;

    @MockitoBean
    private JobApplicationService jobApplicationService;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private JobApplicationResponse jobApplicationResponse;
    private JobApplicationRequest jobApplicationRequest;
    private JobApplicationRequest jobApplicationBadRequest;
    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        jobApplicationResponse = new JobApplicationResponse(
                1L,
                "ING",
                "Java Developer",
                "https://ing.com",
                "Jan de Vries",
                LocalDate.of(2026, 1, 2),
                LocalDate.now(),
                ApplicationStatus.APPLIED,
                "Applied via LinkedIn"
        );

        jobApplicationRequest = new JobApplicationRequest(
                "ING",
                "Java Developer",
                "https://ing.com",
                "Jan de Vries",
                LocalDate.of(2026, 1, 2),
                null,
                ApplicationStatus.APPLIED,
                "Applied via LinkedIn"
        );
        jobApplicationBadRequest = new JobApplicationRequest(null,
                "Java Developer",
                "https://ing.com",
                "Jan de Vries",
                LocalDate.of(2026, 1, 2),
                null,
                ApplicationStatus.APPLIED,
                "Applied via LinkedIn");
    }

    @Test
    void getAllJobApplications_shouldReturn200WithList() throws Exception {
        when(jobApplicationService.getAllJobApplications(USER_ID)).thenReturn(List.of(jobApplicationResponse));

        mockMvc.perform(get("/applications")
                        .header("X-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$[0].id").value(1));
        verify(jobApplicationService).getAllJobApplications(USER_ID);
    }

    @Test
    void getJobApplicationById_shouldReturnJobApplication() throws Exception {
        when(jobApplicationService.getJobApplication(1L, USER_ID)).thenReturn(jobApplicationResponse);

        mockMvc.perform(
                        get("/applications/1")
                                .header("X-User-Id", USER_ID)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
        verify(jobApplicationService).getJobApplication(1L, USER_ID);
    }

    @Test
    void getJobApplicationById_shouldReturn404() throws Exception {
        when(jobApplicationService.getJobApplication(1L, USER_ID)).thenThrow(ResourceNotFoundException.class);
        mockMvc.perform(
                        get("/applications/1")
                                .header("X-User-Id", USER_ID)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        verify(jobApplicationService).getJobApplication(1L, USER_ID);
    }

    @Test
    void createJobApplication_shouldReturn201WhenValid() throws Exception {
        when(jobApplicationService.createJobApplication(any(), anyLong())).thenReturn(jobApplicationResponse);
        mockMvc.perform(
                        post("/applications")
                                .header("X-User-Id", USER_ID)
                                .content(objectMapper.writeValueAsString(jobApplicationRequest))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, "/applications/1"));
        verify(jobApplicationService).createJobApplication(jobApplicationRequest, USER_ID);
    }

    @Test
    void createJobApplication_shouldReturn400() throws Exception {
        mockMvc.perform(
                        post("/applications")
                                .header("X-User-Id", USER_ID)
                                .content(objectMapper.writeValueAsString(jobApplicationBadRequest))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(jobApplicationService);
    }

    @Test
    void updateJobApplication_shouldReturn204() throws Exception {
        mockMvc.perform(
                        put("/applications/1")
                                .header("X-User-Id", USER_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(jobApplicationRequest)))
                .andExpect(status().isNoContent());
        verify(jobApplicationService).updateJobApplication(1L, jobApplicationRequest, USER_ID);
    }

    @Test
    void updateJobApplication_shouldReturn404() throws Exception {
        doThrow(ResourceNotFoundException.class).when(jobApplicationService).updateJobApplication(anyLong(), any(), anyLong());
        mockMvc.perform(
                        put("/applications/1")
                                .header("X-User-Id", USER_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(jobApplicationRequest)))
                .andExpect(status().isNotFound());
        verify(jobApplicationService).updateJobApplication(1L, jobApplicationRequest, USER_ID);
    }

    @Test
    void deleteJobApplication_shouldReturn204WhenDeleted() throws Exception {
        mockMvc.perform(
                        delete("/applications/1")
                                .header("X-User-Id", USER_ID)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        verify(jobApplicationService).deleteJobApplication(1L, USER_ID);
    }

    @Test
    void deleteJobApplication_shouldReturn404WhenNotFound() throws Exception {
        doThrow(ResourceNotFoundException.class).when(jobApplicationService).deleteJobApplication(anyLong(), anyLong());
        mockMvc.perform(
                        delete("/applications/1")
                                .header("X-User-Id", USER_ID)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        verify(jobApplicationService).deleteJobApplication(1L, USER_ID);
    }
}
