package com.jobtracker.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jobtracker.api.dto.auth.AuthRequest;
import com.jobtracker.api.dto.auth.AuthResponse;
import com.jobtracker.api.dto.auth.RegisterRequest;
import com.jobtracker.api.exception.InvalidCredentialsException;
import com.jobtracker.api.exception.UserAlreadyExistsException;
import com.jobtracker.api.security.JwtService;
import com.jobtracker.api.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private UserDetailsService userDetailsService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());
    private RegisterRequest registerRequest;
    private RegisterRequest badRequest;
    private AuthRequest authRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest("test@sample.com", "password", "Test", "Auth");
        badRequest = new RegisterRequest("abc@sample.com", "password", null, "sample");
        authRequest = new AuthRequest("test@example.com", "password");
    }

    @Test
    void registerUser_shouldReturn201() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());
        verify(userService).registerUser(registerRequest);
    }

    @Test
    void registerUser_shouldReturn409() throws Exception {
        doThrow(UserAlreadyExistsException.class).when(userService).registerUser(registerRequest);
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isConflict());
        verify(userService).registerUser(registerRequest);
    }

    @Test
    void registerUser_shouldReturn400() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest)))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(userService);
    }

    @Test
    void loginUser_shouldReturn200() throws Exception {
        when(userService.loginUser(any())).thenReturn(new AuthResponse("token"));
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token"));
        verify(userService).loginUser(authRequest);

    }

    @Test
    void loginUser_shouldReturn401() throws Exception {
        when(userService.loginUser(any())).thenThrow(InvalidCredentialsException.class);
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized());
        verify(userService).loginUser(authRequest);
    }
}