package com.jobtracker.user.service;

import com.jobtracker.user.dto.AuthRequest;
import com.jobtracker.user.dto.AuthResponse;
import com.jobtracker.user.dto.RegisterRequest;
import com.jobtracker.user.exception.InvalidCredentialsException;
import com.jobtracker.user.exception.UnauthorizedException;
import com.jobtracker.user.exception.UserAlreadyExistsException;
import com.jobtracker.user.model.Role;
import com.jobtracker.user.model.User;
import com.jobtracker.user.repository.UserRepository;
import com.jobtracker.jwt.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserService userService;

    private User user;
    private RegisterRequest registerRequest;
    private AuthRequest authRequest;
    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L).password("password")
                .email("test@example.com")
                .build();
        registerRequest = new RegisterRequest(
                "test@example.com",
                "password",
                "Test",
                "User");
        authRequest = new AuthRequest("test@example.com",
                "password");
        authResponse = new AuthResponse("bearer-token");

    }

    @Test
    void registerUser_shouldSaveUserWithEncodedPassword() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        userService.registerUser(registerRequest);
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        verify(passwordEncoder).encode(registerRequest.password());
        User captorValue = captor.getValue();
        assertThat(captorValue.getEmail()).isEqualTo(registerRequest.email());
        assertThat(captorValue.getPassword()).isEqualTo("encodedPassword");
        assertThat(captorValue.getFirstName()).isEqualTo(registerRequest.firstName());
        assertThat(captorValue.getRole()).isEqualTo(Role.ROLE_USER);
    }

    @Test
    void registerUser_shouldThrowWhenEmailAlreadyExists() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        assertThatThrownBy(() -> userService.registerUser(registerRequest))
                .isInstanceOf(UserAlreadyExistsException.class);
    }

    @Test
    void loginUser_shouldReturnTokenWhenCredentialsAreValid() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtService.generateToken(anyString(), any(Long.class))).thenReturn("token");

        AuthResponse response = userService.loginUser(authRequest);

        assertThat(response.token()).isEqualTo("token");
        verify(userRepository).findByEmail(authRequest.email());
        verify(passwordEncoder).matches(authRequest.password(), user.getPassword());
        verify(jwtService).generateToken(authRequest.email(), user.getId());
    }

    @Test
    void loginUser_shouldThrowWhenEmailNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.loginUser(authRequest)).isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void loginUser_shouldThrowWhenPasswordIsInvalid() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
        assertThatThrownBy(() -> userService.loginUser(authRequest)).isInstanceOf(InvalidCredentialsException.class);
    }
}