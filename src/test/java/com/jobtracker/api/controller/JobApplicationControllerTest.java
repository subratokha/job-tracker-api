package com.jobtracker.api.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jobtracker.api.dto.JobApplicationRequest;
import com.jobtracker.api.dto.JobApplicationResponse;
import com.jobtracker.api.exception.ResourceNotFoundException;
import com.jobtracker.api.exception.UnauthorizedException;
import com.jobtracker.api.model.ApplicationStatus;
import com.jobtracker.api.security.JwtService;
import com.jobtracker.api.service.JobApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(JobApplicationController.class)
class JobApplicationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JobApplicationService jobApplicationService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private JobApplicationResponse jobApplicationResponse;
    private JobApplicationRequest jobApplicationRequest;
    private JobApplicationRequest jobApplicationBadRequest;

    @BeforeEach
    void setUp() {
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
    @WithMockUser
    void getAllJobApplications_shouldReturn200WithList() throws Exception {
        when(jobApplicationService.getAllJobApplications()).thenReturn(List.of(jobApplicationResponse));

        mockMvc.perform(get("/applications")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$[0].id").value(1));
        verify(jobApplicationService).getAllJobApplications();
    }

    @Test
    @WithMockUser
    void getJobApplicationById_shouldReturnJobApplication() throws Exception {
        when(jobApplicationService.getJobApplication(anyLong())).thenReturn(jobApplicationResponse);

        mockMvc.perform(
                        get("/applications/1")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
        verify(jobApplicationService).getJobApplication(1L);
    }

    @Test
    @WithMockUser
    void getJobApplicationById_shouldReturn404() throws Exception {
        when(jobApplicationService.getJobApplication(anyLong())).thenThrow(ResourceNotFoundException.class);
        mockMvc.perform(
                        get("/applications/1")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        verify(jobApplicationService).getJobApplication(1L);
    }

    @Test
    @WithMockUser
    void createJobApplication_shouldReturn201WhenValid() throws Exception {
        when(jobApplicationService.createJobApplication(any())).thenReturn(jobApplicationResponse);
        mockMvc.perform(
                        post("/applications")
                                .content(objectMapper.writeValueAsString(jobApplicationRequest))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, "/applications/1"));
        verify(jobApplicationService).createJobApplication(jobApplicationRequest);
    }

    @Test
    @WithMockUser
    void createJobApplication_shouldReturn400() throws Exception {
        mockMvc.perform(
                        post("/applications")
                                .content(objectMapper.writeValueAsString(jobApplicationBadRequest))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(jobApplicationService);
    }

    @Test
    @WithMockUser
    void createJobApplication_shouldReturn401() throws Exception {
        when(jobApplicationService.createJobApplication(any())).thenThrow(UnauthorizedException.class);
        mockMvc.perform(
                        post("/applications")
                                .content(objectMapper.writeValueAsString(jobApplicationRequest))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        verify(jobApplicationService).createJobApplication(jobApplicationRequest);
    }

    @Test
    @WithMockUser
    void updateJobApplication_shouldReturn204() throws Exception {
        mockMvc.perform(
                        put("/applications/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(jobApplicationRequest)))
                .andExpect(status().isNoContent());
        verify(jobApplicationService).updateJobApplication(1L, jobApplicationRequest);

    }

    @Test
    @WithMockUser
    void updateJobApplication_shouldReturn404() throws Exception {
        doThrow(ResourceNotFoundException.class).when(jobApplicationService).updateJobApplication(anyLong(), any());
        mockMvc.perform(
                        put("/applications/1").contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(jobApplicationRequest)))
                .andExpect(status().isNotFound());
        verify(jobApplicationService).updateJobApplication(1L, jobApplicationRequest);
    }

    @Test
    @WithMockUser
    void deleteJobApplication_shouldReturn204WhenDeleted() throws Exception {
        mockMvc.perform(
                        delete("/applications/1")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        verify(jobApplicationService).deleteJobApplication(1L);
    }

    @Test
    @WithMockUser
    void deleteJobApplication_shouldReturn401WhenUnauthorized() throws Exception {
        doThrow(UnauthorizedException.class).when(jobApplicationService).deleteJobApplication(anyLong());
        mockMvc.perform(
                        delete("/applications/1")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        verify(jobApplicationService).deleteJobApplication(1L);
    }

}