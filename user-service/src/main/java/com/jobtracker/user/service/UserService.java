package com.jobtracker.user.service;


import com.jobtracker.jwt.JwtService;
import com.jobtracker.user.dto.AuthRequest;
import com.jobtracker.user.dto.AuthResponse;
import com.jobtracker.user.dto.RegisterRequest;
import com.jobtracker.user.exception.InvalidCredentialsException;
import com.jobtracker.user.exception.UnauthorizedException;
import com.jobtracker.user.exception.UserAlreadyExistsException;
import com.jobtracker.user.model.Role;
import com.jobtracker.user.model.User;
import com.jobtracker.user.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public void registerUser(RegisterRequest registerRequest) {
        Optional<User> userPresent = userRepository.findByEmail(registerRequest.email());
        if (userPresent.isPresent()) {
            throw new UserAlreadyExistsException("User already exists");
        }
        userRepository.save(mapToEntity(registerRequest));
    }

    public AuthResponse loginUser(AuthRequest authRequest) {
        User user = userRepository.findByEmail(authRequest.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(authRequest.password(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }
        String token = jwtService.generateToken(user.getEmail(), user.getId());
        return new AuthResponse(token);
    }

    private User mapToEntity(RegisterRequest registerRequest) {
        return User.builder()
                .email(registerRequest.email())
                .password(passwordEncoder.encode(registerRequest.password()))
                .firstName(registerRequest.firstName())
                .lastName(registerRequest.lastName())
                .role(Role.ROLE_USER)
                .build();
    }
}
