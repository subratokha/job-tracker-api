package com.jobtracker.api.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {
    @InjectMocks
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtService, "secret", "3d8f2b1c9e4a7f6d0b5c8e2a1f9d4b7c3e6a0f8d2b5c9e1a4f7d0b3c6e9a2f5");
        ReflectionTestUtils.setField(jwtService, "expiration", 3600000L);
    }

    @Test
    void generateToken_shouldReturnValidToken() {
        String generatedToken = jwtService.generateToken("test@sample.com");
        assertThat(generatedToken).isNotBlank();
        assertThat(generatedToken.split("\\.")).hasSize(3);
    }

    @Test
    void validateToken_shouldReturnTrueForValidToken() {
        String token = jwtService.generateToken("test@sample.com");
        boolean result = jwtService.validateToken(token, "test@sample.com");
        assertTrue(result);
    }

    @Test
    void validateToken_shouldReturnFalseWhenEmailDoesntMatch() {
        String token = jwtService.generateToken("test@sample.com");
        boolean result = jwtService.validateToken(token, "abc@sample.com");
        assertFalse(result);
    }

    @Test
    void validateToken_shouldReturnFalseForExpiredToken() {
        ReflectionTestUtils.setField(jwtService, "expiration", -1000L); // already expired
        String token = jwtService.generateToken("test@sample.com");
        boolean result = jwtService.validateToken(token, "test@sample.com");
        assertFalse(result);
    }

    @Test
    void extractEmailFromToken_shouldReturnCorrectEmail() {
        String jwt = jwtService.generateToken("test@sample.com");
        String email = jwtService.extractEmailFromToken(jwt);
        assertThat(email).isEqualTo("test@sample.com");
    }
}