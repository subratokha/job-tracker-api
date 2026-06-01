package com.jobtracker.api.service;

import com.jobtracker.api.dto.auth.AuthRequest;
import com.jobtracker.api.dto.auth.AuthResponse;
import com.jobtracker.api.dto.auth.RegisterRequest;
import com.jobtracker.api.exception.InvalidCredentialsException;
import com.jobtracker.api.exception.UnauthorizedException;
import com.jobtracker.api.exception.UserAlreadyExistsException;
import com.jobtracker.api.model.Role;
import com.jobtracker.api.model.User;
import com.jobtracker.api.repository.UserRepository;
import com.jobtracker.api.security.JwtService;
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
        String token = jwtService.generateToken(authRequest.email());
        return new AuthResponse(token);
    }

    public User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException();
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(UnauthorizedException::new);
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
