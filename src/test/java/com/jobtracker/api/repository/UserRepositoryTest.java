package com.jobtracker.api.repository;

import com.jobtracker.api.model.Role;
import com.jobtracker.api.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TestEntityManager testEntityManager;

    private User user1;

    @BeforeEach
    void setUp() throws Exception {
        user1 = User.builder()
                .email("test@example.com")
                .password("password")
                .role(Role.ROLE_USER)
                .build();

        testEntityManager.persistAndFlush(user1);
    }

    @Test
    void findByEmail_returnsUserWhenExists() {
        Optional<User> existingUser = userRepository.findByEmail("test@example.com");
        assertThat(existingUser).isPresent();
        assertThat(existingUser.get().getEmail()).isEqualTo(user1.getEmail());
    }

    @Test
    void findByEmail_returnsEmptyWhenUserDoesNotExist() {
        Optional<User> existingUser = userRepository.findByEmail("xyz@example.com");
        assertThat(existingUser).isEmpty();
    }
}