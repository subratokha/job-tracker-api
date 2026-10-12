package com.jobtracker.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jobtracker.user.dto.AuthRequest;
import com.jobtracker.user.dto.AuthResponse;
import com.jobtracker.user.dto.RegisterRequest;
import com.jobtracker.user.exception.GlobalExceptionHandler;
import com.jobtracker.user.exception.InvalidCredentialsException;
import com.jobtracker.user.exception.UserAlreadyExistsException;
import com.jobtracker.jwt.JwtService;
import com.jobtracker.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;
    @Mock
    private JwtService jwtService;
    @InjectMocks
    private AuthController authController;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());
    private RegisterRequest registerRequest;
    private RegisterRequest badRequest;
    private AuthRequest authRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
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